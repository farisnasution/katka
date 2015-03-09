(defproject katka "0.1.0"
  :description "Finals"
  :url "https://github.com/farisnasution/katka"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3030"]
                 [figwheel "0.2.2-SNAPSHOT"]
                 [hiccup "1.0.5"]
                 [org.omcljs/om "0.8.8"]
                 [sablono "0.3.4"]
                 [secretary "1.2.1"]]
  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-figwheel "0.2.2-SNAPSHOT"]
            [hiccup-watch "0.1.1"]
            [com.cemerick/clojurescript.test "0.3.1"]
            [lein-haml-sass "0.2.7-SNAPSHOT"]]
  :figwheel {:http-server-root "public"
             :port 3449
             :css-dirs ["resources/public/css"]}
  :jvm-opts ["-Xmx1G"]
  :aliases {"dev" ["figwheel" "dev"]
            "omni" ["do" ["ancient"] ["kibit"] ["eastwood"] ["bikeshed"]]}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"
                                       "src/figwheel"]
                        :compiler {:output-to "resources/public/js/katka.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "prod"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/katka_prod.js"
                                   :output-dir "resources/public/js/out_prod"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :source-map "resources/public/js/katka_prod.js.map"}}]}
  :hiccup-watch {:input-dir "src/hiccup/katka"
                 :output-dir "resources/public"}
  :sass {:src "src/sass/katka"
         :output-directory "resources/public/css"
         :output-extension "css"}
  :eastwood {:exclude-linters [:unlimited-use]
             :exclude-namespaces [:test-paths]})
