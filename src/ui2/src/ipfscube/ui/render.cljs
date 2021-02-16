(ns ipfscube.ui.render
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [reagent.core :as r]
   [reagent.dom :as rdom]

   [ipfscube.daemon.spec :as daemon.spec]
   [ipfscube.daemon.chan :as daemon.chan]
   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [ipfscube.ui.spec :as ui.spec]
   [ipfscube.ui.chan :as ui.chan]

   ["react" :as React :refer [useEffect]]
   ["react-router-dom" :as ReactRouter :refer [BrowserRouter
                                               HashRouter
                                               Switch
                                               Route
                                               Link
                                               useLocation
                                               useHistory
                                               useRouteMatch
                                               useParams]]
   ["antd/lib/layout" :default AntLayout]
   ["antd/lib/menu" :default AntMenu]
   ["antd/lib/icon" :default AntIcon]
   ["antd/lib/button" :default AntButton]
   ["antd/lib/list" :default AntList]
   ["antd/lib/row" :default AntRow]
   ["antd/lib/col" :default AntCol]
   ["antd/lib/form" :default AntForm]
   ["antd/lib/input" :default AntInput]
   ["antd/lib/tabs" :default AntTabs]
   ["antd/lib/table" :default AntTable]

   ["antd/lib/checkbox" :default AntCheckbox]


   ["antd/lib/divider" :default AntDivider]
   ["@ant-design/icons/SmileOutlined" :default AntIconSmileOutlined]
   ["@ant-design/icons/LoadingOutlined" :default AntIconLoadingOutlined]
   ["@ant-design/icons/SyncOutlined" :default AntIconSyncOutlined]
   ["@ant-design/icons/ReloadOutlined" :default AntIconReloadOutlined]))

(do
  (def ant-row (reagent.core/adapt-react-class AntRow))
  (def ant-col (reagent.core/adapt-react-class AntCol))
  (def ant-divider (reagent.core/adapt-react-class AntDivider))
  (def ant-layout (reagent.core/adapt-react-class AntLayout))
  (def ant-layout-content (reagent.core/adapt-react-class (.-Content AntLayout)))
  (def ant-layout-header (reagent.core/adapt-react-class (.-Header AntLayout)))

  (def ant-menu (reagent.core/adapt-react-class AntMenu))
  (def ant-menu-item (reagent.core/adapt-react-class (.-Item AntMenu)))
  (def ant-icon (reagent.core/adapt-react-class AntIcon))
  (def ant-button (reagent.core/adapt-react-class AntButton))
  (def ant-button-group (reagent.core/adapt-react-class (.-Group AntButton)))
  (def ant-list (reagent.core/adapt-react-class AntList))
  (def ant-input (reagent.core/adapt-react-class AntInput))
  (def ant-input-password (reagent.core/adapt-react-class (.-Password AntInput)))
  (def ant-checkbox (reagent.core/adapt-react-class AntCheckbox))
  (def ant-form (reagent.core/adapt-react-class AntForm))
  (def ant-table (reagent.core/adapt-react-class AntTable))
  (def ant-form-item (reagent.core/adapt-react-class (.-Item AntForm)))
  (def ant-tabs (reagent.core/adapt-react-class AntTabs))
  (def ant-tab-pane (reagent.core/adapt-react-class (.-TabPane AntTabs)))

  (def ant-icon-smile-outlined (reagent.core/adapt-react-class AntIconSmileOutlined))
  (def ant-icon-loading-outlined (reagent.core/adapt-react-class AntIconLoadingOutlined))
  (def ant-icon-sync-outlined (reagent.core/adapt-react-class AntIconSyncOutlined))
  (def ant-icon-reload-outlined (reagent.core/adapt-react-class AntIconReloadOutlined)))

(defn create-state*
  [data]
  (reagent.core/atom data))

(declare  rc-main
          rc-page-main
          rc-page-foo
          rc-page-foo-name
          rc-page-not-found)

(defn render-ui
  [channels state* {:keys [id] :or {id "ui"}}]
  (reagent.dom/render [:f> rc-main channels state*]  (.getElementById js/document id)))

; https://github.com/reagent-project/reagent/blob/master/CHANGELOG.md
; https://github.com/reagent-project/reagent/blob/master/examples/functional-components-and-hooks/src/example/core.cljs
; https://github.com/reagent-project/reagent/blob/master/doc/ReagentCompiler.md
; https://github.com/reagent-project/reagent/blob/master/doc/ReactFeatures.md

(defn rc-main
  [channels state*]
  (r/with-let
    []
    [:> #_BrowserRouter HashRouter
     [:> Switch
      [:> Route {"path" "/"
                 "exact" true}
       [:f> rc-page-main channels state*]]
      [:> Route {"path" "/foo/:name"}
       [:f> rc-page-foo-name channels state*]]
      [:> Route {"path" "/foo"
                 "exact" true}
       [:f> rc-page-foo channels state*]]
      [:> Route {"path" "*"}
       [:f> rc-page-not-found channels state*]]]]))

(defn menu
  [channels state*]
  (reagent.core/with-let
    []
    (let [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                         :keywordize-keys true)]
      [ant-menu {:theme "light"
                 :mode "horizontal"
                 :size "small"
                 :style {:lineHeight "32px"}
                 :default-selected-keys ["home-panel"]
                 :selected-keys [path]
                 :on-select (fn [x] (do))}
       [ant-menu-item {:key "/"}
        [:r> Link #js {:to "/"} "main"]]
       [ant-menu-item {:key "/foo"}
        [:r> Link #js  {:to "/foo"} "foo"]]
       [ant-menu-item {:key "/foo/:name"}
        [:r> Link #js  {:to (format "/foo/%s" (subs (str (random-uuid)) 0 7))} "/foo/:name"]]])))

(defn layout
  [channels state* content]
  [ant-layout {:style {:min-height "100vh"}}
   [ant-layout-header
    {:style {:position "fixed"
             :z-index 1
             :lineHeight "32px"
             :height "32px"
             :padding 0
             :background "#000" #_"#001529"
             :width "100%"}}
    [:div {:href "/"
           :class "ui-logo"}
     #_[:img {:class "logo-img" :src "./img/logo-4.png"}]
     [:div {:class "logo-name"} "http://github.com/ipfs-shipyard/cube"]]
    [:f> menu channels state*]]
   [ant-layout-content {:class "main-content"
                        :style {:margin-top "32px"
                                :padding "32px 32px 32px 32px"}}
    content]])


(defn rc-page-main
  [channels state*]
  (reagent.core/with-let
    [_ (useEffect (fn []
                    (println ::rc-page-main-mount)
                    (fn useEffect-cleanup []
                      (println ::rc-page-main-unmount))))]
    [layout channels state*
     [:<>
      [:div ::rc-page-main]
      
      #_[:<>
         (if (empty? @state*)

           [:div "loading..."]

           [:<>
            [:pre {} (with-out-str (pprint @state*))]
            [ant-button {:icon (reagent.core/as-element [ant-icon-sync-outlined])
                         :size "small"
                         :title "button"
                         :on-click (fn [] ::button-click)}]])]]]))


(defn rc-page-foo-name
  [channels state*]
  (reagent.core/with-let
    []
    (let [{:keys [:path :url :isExact :params]} (js->clj (useRouteMatch)
                                                         :keywordize-keys true)
          _ (useEffect (fn []
                         (do ::mount-smth)
                         (fn []
                           (do ::unmount-it))))]
      [layout channels state*
       [:div (str "/foo/" (:name params))]])))


(defn rc-page-foo
  [channels state*]
  (reagent.core/with-let
    []
    (let []
      [layout channels state*
       [:<>
        [:div "rc-page-foo"]
        [:div
         [ant-row {}
          [ant-col
           [:r> Link #js  {:to "/foo/bar"} "/foo/bar"]]]
         [ant-row {}
          [ant-col
           [:r> Link #js  {:to "/foo/baz"} "/foo/baz"]]]]]])))


(defn rc-page-not-found
  [channels state*]
  (reagent.core/with-let
    [layout channels state*
     [:<>
      [:div ::rc-page-not-found]]]))


