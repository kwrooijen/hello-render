FROM clojure:openjdk-8-lein-slim-buster AS build-jar
# Install any dependencies e.g. NPM for Clojurescript
# RUN apt update && apt install npm -y
WORKDIR /usr/src
COPY . .

# Build the uberjar. If you need to prepare certain things for your release
# (e.g. build your Clojurescript with Shadow-cljs) you can add a `:prep-tasks`
# key to the `:uberjar` profile in your `project.clj`. `lein-shell` can also be
# used to execute shell commands such as `npm`.
#
# >>> project.clj
#
# :plugins [[lein-shell "0.5.0"]]
# :profiles
# {:uberjar {:aot :all
#            :prep-tasks [["shell" "npm" "install"]
#                         ["shell" "shadow-cljs release app"]]}}
RUN lein uberjar

# By creating a second stage for our Dockerfile, the release image will be
# significantly smaller in size. This will be very beneficial for uploading the
# image to Render's Docker registry.
FROM openjdk:8-jre-alpine
WORKDIR /usr/app

# Add your uberjar name here
COPY --from=build-jar /usr/src/target/hello-render.jar .
CMD ["java", "-jar", "hello-render.jar"]
