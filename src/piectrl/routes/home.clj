(ns piectrl.routes.home
  (:require [piectrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :refer [response]]
            [clojure.java.io :as io]
            [piectrl.webiopirest :as webiorest]))

(defn home-page []
  (layout/render "home.html"))

(defn get-state [id]
    (response {:id  id
               :ttl @webiorest/pi-ttl
               :status (if (and
                            (=((first @webiorest/pi-atom):status) "1")
                            (=((last @webiorest/pi-atom):status) "0"))
                        "1" "0")}))

(defn set-state [id ttl status]
   (response (webiorest/update-state id ttl status)))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/get-state" [id] (get-state id))
  (POST "/set-state" [id ttl status] (set-state id ttl status)))
