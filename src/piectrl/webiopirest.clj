(ns piectrl.webiopirest
  (:use [overtone.at-at])
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))
;; ############################################
;; constants and atoms
;; ############################################
(def pi-info (env :pi-info))

(def pi-ttl (atom 0))
;; ############################################
;; Build and send requests
;; ############################################
(defn req-build [id &[newVal]]
  (str/join "" [(pi-info :url) "GPIO/" id "/value" newVal]))

(defn req-send [fn url]
  (try
    (fn url {:basic-auth [(pi-info :user) (pi-info :password)]})
  (catch Exception e
    ((log/error (str/join " " ["There was an issue connecting to" url]))
               {:body -1}))))

  (defn get-GPIO-status [id]
   {:id id
    :status ((req-send client/get (req-build id)) :body)})

(defn get-all-GPIO [] (map #(get-GPIO-status %1) (pi-info :GPIOs)))

(defn def-mapper [id] {:id id :status 0})
(def pi-atom (atom (map #(def-mapper %1) (pi-info :GPIOs))))

(defn update-pi-atom []
  (reset! pi-atom (get-all-GPIO))
  (log/debug "updating from pi source"))

(defn set-GPIO [id state]
  ((req-send client/post
              (req-build id (str/join "" ["/" state]))) :body)
  (update-pi-atom))

(defn turn-off-all [] (set-GPIO 17 0))

;; ############################################
;; Timer function
;; ############################################
(defn reset-pool [pool] (stop-and-reset-pool! pool))
(defn show-sched [pool] (show-schedule pool))
(def kill-pool (mk-pool))
(def update-pool (mk-pool))
(defn start-updater [] (every 5000 #(update-pi-atom) update-pool)
      (log/debug "updater should be started"))

(defn send-kill [] (println "sending off sig")
  (reset-pool kill-pool)(reset! pi-ttl 0)
  (turn-off-all))

(defn death-task [ms-tl]
  (at (+ ms-tl (now)) (send-kill) kill-pool))

;; ############################################
;; UI function
;; ############################################
(defn set-ttl-int [new-val id]
  (reset! pi-ttl (+ (* 60000 (read-string new-val))(System/currentTimeMillis)))
  (reset-pool kill-pool)(death-task @pi-ttl)(set-GPIO id 1))

(defn update-state [id new-val status]
  (if (= new-val "0")
        ((if (= status 0)(send-kill)((set-GPIO id 1)(reset! pi-ttl 0))))
    ; If new ttl is not 0 update interal timer
        (set-ttl-int new-val id)))
