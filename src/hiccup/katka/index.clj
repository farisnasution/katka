[:html
 [:head
  [:title {}
   Katka]
  [:link {:rel "stylesheet"
          :href "css/vendor/bootstrap-3.3.2.min.css"}]
  [:link {:rel "stylesheet"
          :href "css/katka.css"}]]
 [:body
  [:div {:id "for-navbar"}]
  [:p {:id "my-app"}]
  [:script {:src "js/out/goog/base.js"
            :type "text/javascript"}]
  [:script {:src "js/vendor/d3.v3.min.js"
            :type "text/javascript"}]
  [:script {:src "js/katka.js"
            :type "text/javascript"}]
  [:script {:type "text/javascript"}
   "goog.require('katka.core')"]
  [:script {:type "text/javascript"}
   "goog.require('katka.katka_figwheel')"]]]
