(ns piectrl.webiopirest
   (:require [clj-http.client :as client]
             [environ.core :refer [env]]
             [clojure.string :as str]))
;; ############################################
;; constants and atoms
;; ############################################
(def pi-info (env :pi-info))

(def pi-ttl (atom (System/currentTimeMillis) 1))

;; ############################################
;; Build and send requests
;; ############################################
(defn req-build [id &[newVal]]
  (str/join "" [(pi-info :url) "GPIO/" id "/value" newVal]))

(defn req-send [fn url] (fn url
  {:basic-auth [(pi-info :user) (pi-info :password)]}))

  (defn get-GPIO-status [id]
   {:id id
    :status ((req-send client/get (req-build id)) :body)})


(defn get-all-GPIO [] (map #(get-GPIO-status %1) (pi-info :GPIOs)))

(def pi-atom (atom (get-all-GPIO)))

(defn update-pi-atom [] (swap! pi-atom (get-all-GPIO))
  (println @pi-atom))

(defn set-GPIO [id state]
   ((req-send client/post
              (req-build id (str/join "" ["/" state]))) :body))

