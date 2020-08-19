(ns hello-render.main
  (:gen-class)
  (:require
   [aleph.http :as http]
   [reitit.ring :as ring]
   [ring.middleware.defaults :refer [wrap-defaults]]
   [hikari-cp.core :as hikari-cp]
   [clj-database-url.core :refer [jdbc-database-url]]))

;; Handlers

(defonce datasource
  (delay (hikari-cp/make-datasource
          {:jdbc-url (jdbc-database-url (System/getenv "DATABASE_URL"))})))

(defn home-handler [request]
  {:status 200
   :body (str "Hello world!" @datasource)})

;; Routes and middleware

(def routes
  [["/" {:get {:handler home-handler}}]])

(def ring-opts
  {:data
   {:middleware
    [#(wrap-defaults % ring.middleware.defaults/api-defaults)]}})

(def app
  (ring/ring-handler
   (ring/router routes ring-opts)))

;; Web server

(defonce server (atom nil))

(def port (-> (System/getenv "PORT")
              (or "3000")
              (Integer/parseInt)))

(defn start-server []
  (reset! server (http/start-server #'app {:port port})))

(defn stop-server []
  (when @server
    (.close ^java.io.Closeable @server)))

(defn restart-server []
  (stop-server)
  (start-server))

;; Application entrypoint

(defn -main [& args]
  (println "Starting webserver.")
  (start-server))
