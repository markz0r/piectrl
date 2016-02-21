(ns piectrl.routes.home
  (:require [piectrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :refer [response]]
            [clojure.java.io :as io]
            [piectrl.webiopirest :as webiorest]))

(defn home-page []
  (layout/render "home.html"))

(defn get-ttl [id]
    (response {:id  id
               :ttl @webiorest/pi-ttl}))

(defn set-ttl [id ttl]
   (response (webiorest/update-ttl id ttl)))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/get-ttl" [id] (get-ttl id))
  (POST "/set-ttl" [id ttl] (set-ttl id ttl)))
