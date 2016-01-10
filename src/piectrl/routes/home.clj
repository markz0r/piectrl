(ns piectrl.routes.home
  (:require [piectrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :refer [response]]
            [clojure.java.io :as io]
            [piectrl.timer :as timer]))

(defn home-page []
  (layout/render "home.html"))

(defn get-timer-data [id]
    (response {:foo "getbar"}))

(defn set-timer-data [id minval]
   (response {:foo "setbar"}))


(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/get-timer-data" [id] (get-timer-data id))
  (POST "/set-timer-data" [id minval] (set-timer-data id minval))
  )

