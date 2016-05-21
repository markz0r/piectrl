(ns piectrl.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(def timer-data (reagent/atom {:ttl 0 :status 0 :locked 0 :connection false}))
;; ############################################
;; AJAX
;; ############################################

(defn handler [response] (.log js/console (str response))
    (if (= (response :status) "0")
            (reset! timer-data {:ttl 0 :status 0 :locked 0 :connection true})
          (if (= (response :status) "1")
                (if (= (response :ttl) 0)
                  (reset! timer-data {:ttl 0 :status 1 :locked 0 :connection true})
                  (reset! timer-data
                      {:ttl (.toFixed ( / ( - (response :ttl) (.getTime (js/Date.))) 60000)) :status 1 :locked 0 :connection true}))
                (reset! timer-data :connection false))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console
    (str "something bad happened: " status " " status-text)))

(defn send-post [loc params-list] (POST loc
        {:headers {"Accept" "application/transit+json"
                   "x-csrf-token" (.-value (.getElementById js/document "csrf-token"))}
         :params params-list
         :handler handler
         :error-handler error-handler}))

(defn send-get [loc] (GET loc
        {:headers {"Accept" "application/transit+json"
                   "x-csrf-token" (.-value (.getElementById js/document "csrf-token"))}
         :handler handler
         :error-handler error-handler}))

(defn set-status-data [id ttl status] (send-post "/set-state"
                                 {:id id
                                  :ttl (str ttl)
                                  :status status}))

(defn get-status-data [id] (if (= (@timer-data :locked) 0)
                            (send-post "/get-state"
                                {:id id})))

(defonce data-updater (js/setInterval #(get-status-data 17) 5000))
;; ############################################
;; COMPONENTS
;; ############################################
(defn slider [param min max id]
  [:input {:type "range" :value (@timer-data :ttl) :min min :max max :id id
           :visible? (@timer-data :connection)
           :disabled (not (@timer-data :connection))
           :style {:width "95%" :text-align "center"}
           :on-mouse-down #(swap! timer-data assoc :locked 1)
           :on-change #(.log js/console (-> % .-target .-value)
                        (swap! timer-data assoc :ttl (-> % .-target .-value)))
           :on-mouse-up #((swap! timer-data assoc :ttl (-> % .-target .-value))
                          (swap! timer-data assoc :locked 0)
                          (set-status-data id (@timer-data :ttl)(@timer-data :status)))}])

(defn switcher [id]
  [:input {:type "button" :value (if (= (@timer-data :status) 1)"SWITCH OFF" "SWITCH ON") :id id
           :style {:width "20%" :text-align "center"}
           :hidden? (@timer-data :connection)
           :disabled (not (@timer-data :connection))
           :on-mouse-up #((swap! timer-data assoc :status (if (= (@timer-data :status) 1) 0 1))
                          (set-status-data id 0 (@timer-data :status)))}])

(defn nav-link [uri title page collapsed?]
  [:li {:class (when (= page (session/get :page)) "active")}
   [:a {:href uri
        :on-click #(reset! collapsed? true)}
    title]])

(defn navbar []
  (let [collapsed? (atom true)]
    (fn []
      [:nav.navbar.navbar-inverse.navbar-fixed-top
       [:div.container
        [:div.navbar-header
         [:button.navbar-toggle
          {:class         (when-not @collapsed? "collapsed")
           :data-toggle   "collapse"
           :aria-expanded @collapsed?
           :aria-controls "navbar"
           :on-click      #(swap! collapsed? not)}
          [:span.sr-only "Toggle Navigation"]
          [:span.icon-bar]
          [:span.icon-bar]
          [:span.icon-bar]]
         [:a.navbar-brand {:href "#/"} "piectrl"]]
        [:div.navbar-collapse.collapse
         (when-not @collapsed? {:class "in"})
         [:ul.nav.navbar-nav
          [nav-link "#/" "Home" :home collapsed?]]
       [:a.logout-butt {:href "/logout"} "logout"]]]])))

;; ############################################
;; PAGES
;; ############################################
(defn home-page []
  [:div.container
   [:div.jumbotron
   ;; add dials n shit
    [:h4 "Sprinkler"]
    (if (@timer-data :connection)
    [:div
      [:div  (@timer-data :ttl) " mins remaining" [slider timer-data 0 180 17]]
      [:div [switcher 17]]]
    [:div [:h5 "Trying to connect to pi - please wait and ensure pi is powered" ]])]])

(def pages
  {:home #'home-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  ;;(session/put! :identity js/identity)
  (mount-components))
