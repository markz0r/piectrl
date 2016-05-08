(ns piectrl.routes.base
  (:require [piectrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [clojure.java.io :as io]
            [environ.core :refer [env]]))

(defn login-page [& message]
  (layout/render "login.html" {:message (first message)}))

(def authdata
  {:username ((env :pi-info):user)
   :password ((env :pi-info):password)})

(defn handle-login [{:keys [session]} username password]
    (if (and(= username (authdata :username)) (= password (authdata :password)))
        (-> (redirect "/")(assoc :session (assoc session :identity username)))
      (login-page "Invalid credentials!")))

(defn handle-logout []
  (-> (login-page "You have been logged out!")(assoc :session nil)))

(defroutes base-routes
  (GET "/login" [] (login-page))
  (POST "/login" [request username password] (handle-login request username password))
  (GET "/logout" [] (handle-logout)))
