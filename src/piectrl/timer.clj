(ns piectrl.timer
   (:use [overtone.at-at]))

;; ############################################
;; Test
;; ############################################

(def my-pool (mk-pool))

(defn death-task [ms-tl]
  (at (+ ms-tl (now)) #(println "hello from the past!") my-pool))
 
(defn get-tasks [] (show-schedule my-pool))

(defn reset-tasks [] (stop-and-reset-pool! my-pool))
