(ns nurblizer.core
    (:gen-class)
    (:use compojure.core)
    (:require
      [clojure.string :as str]
      [clostache.parser :as clostache]
      [ring.adapter.jetty :as ring]
      [compojure.route :as route]
      [compojure.handler :as handler]))

  ;; main nurble stuff
  (def nouns                                                     ; map nouns into a set
    (set (map (comp str/trim str/upper-case)                     ; upper-case and trim lines
              (-> (slurp (clojure.java.io/resource "nouns.txt")) ; load in nouns.txt
                  (str/split #"\n")))))                          ; split by line



  (def nurble-replacement-text "<span class=\"nurble\">nurble</span>")

  (defn nurble-word [word]
    (get nouns (str/upper-case word) nurble-replacement-text)) ; return word if word in set else nurble

  (defn normalize [text]
    (str/replace text #"[^a-zA-Z ]" ""))

  (defn nurble [text]
    (str/replace (normalize text) 
      #"\n|\w+" #(case %             ; using anon func literal, switch on arg
                  "\n" "<br>"        ; when arg is newline replace with br
                  (nurble-word %)))) ; otherwise nurble the argument (a word)

  ;; webserver stuff
  (defn read-template [template-file]
    (slurp (clojure.java.io/resource (str "templates/" template-file ".mustache"))))

  (defn render
    ([template-file params]
     (clostache/render (read-template template-file) params
                       {:_header (read-template "_header")
                        :_footer (read-template "_footer") })))

  (defroutes main-routes
    (GET "/"        []     (render "index" {}))
    (POST "/nurble" [text] (render "nurble" {:text (nurble text)}))
    (route/resources "/static"))

  (defn -main []
    (ring/run-jetty (handler/site main-routes) {:port 9000}))
