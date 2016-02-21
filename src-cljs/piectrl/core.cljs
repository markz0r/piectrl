(ns piectrl.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(defonce timer-data (reagent/atom 0))

;; ############################################
;; AJAX
;; ############################################
;(def ts (reagent/atom (timer.get-ts)))
;(ajaxer/set-timer-data 17 22)


(defn handler [response] (.log js/console (str response))
(reset! timer-data (response :ttl)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console
    (str "something bad happened: " status " " status-text)))

(defn send-post [loc params-list] (POST loc
        {:headers {"Accept" "application/transit+json"
                   "x-csrf-token" (.-value (.getElementById js/document "csrf-token"))}
         :params params-list
         :handler handler
         :error-handler error-handler}))

(defn set-timer-data [id ttl] (send-post "/set-ttl"
                                 {:id id
                                  :ttl ttl}))

(defn get-timer-data [id] (send-post "/get-ttl"
                                {:id id}))

(def data-updater (js/setInterval
                       #(get-timer-data 17)5000))
                         ;reset! timer (js/Date.)) 5000))
;; ############################################
;; COMPONENTS
;; ############################################
(defn slider [param value min max id]
  [:input {:type "range" :value value :min min :max max :id id
           :style {:width "80%" :text-align "center"}
           :on-change #(reset! timer-data (-> % .-target .-value))
           :on-mouse-up #(set-timer-data id @timer-data);(js/alert id)
          }])

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
          [nav-link "#/" "Home" :home collapsed?]
          [nav-link "#/about" "About" :about collapsed?]]]]])))

;; ############################################
;; PAGES
;; ############################################
(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "blah bla"]]])

(defn home-page []
  [:div.container
   [:div.jumbotron
    [:h1 "Welcome to piectrl"]
    [:p "Control Mr.Pi!"]]]
  [:div.container
   [:div.jumbotron
   ;; add dials n shit
     [:div
      "Sprinkler: " @timer-data " mins remaining"
      [slider timer-data @timer-data 0 180 17]]
    ]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

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
  (mount-components))
