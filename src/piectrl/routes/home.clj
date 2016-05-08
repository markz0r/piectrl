(ns piectrl.routes.home
  (:require [piectrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :refer [response]]
            [clojure.java.io :as io]
            [piectrl.webiopirest :as webiorest]))

(defn home-page []
  (webiorest/update-pi-atom)
    (layout/render "home.html"))

(defn get-state [id]
    (webiorest/update-pi-atom)
    (response {:id  id
               :ttl @webiorest/pi-ttl
               :status (if (= "-1" ((first @webiorest/pi-atom):status))
                         "-1"
                       (if (and
                            (=((first @webiorest/pi-atom):status) "1")
                            (=((last @webiorest/pi-atom):status) "0"))
                        "1" "0"))}))

(defn set-state [id ttl status]
  (webiorest/update-state id ttl status)
  (get-state id))

(defroutes home-routes
    (GET "/" [] (home-page))
    (POST "/get-state" [id] (get-state id))
    (POST "/set-state" [id ttl status] (set-state id ttl status)))
