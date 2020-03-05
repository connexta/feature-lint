(ns com.connexta.feature-lint
  (:require [clojure.pprint :refer [pprint]]
            [clojure.xml :as xml]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (org.apache.maven.plugin MojoExecutionException))
  (:gen-class
   :name com.connexta.FeatureLint
   :main false
   :methods [^:static [lint [String] void]]))


(defn by-tags
  "Predicate that returns true if the tags provided are found in the xml under test."
  [tags]
  (fn [xml] (tags (:tag xml))))

(defn join [a b]
  (set/union
   (if-not (set? a) #{a} a)
   (if-not (set? b) #{b} b)))

(defn join-content [content]
  (cond
    (= (count content) 1)
    (first content)

    (every? map? content)
    (reduce #(merge-with join %1 %2) {} content)

    :else content))

(defn xml->map [xml]
  (if (string? xml)
    xml
    (merge (:attrs xml)
           {(:tag xml)
            (join-content
             (map xml->map (:content xml)))})))

(defn parse-if-exists
  "Parses an xml file if it exists."
  [& path]
  (let [path (.getPath (apply io/file path))]
    (if (.exists (io/file path))
      (xml/parse path))))

(defn main
  "Main entry point to the library."
  [project-directory]
  (let [pom-xml (parse-if-exists project-directory "pom.xml")
        features-xml (parse-if-exists project-directory "src" "main" "resources" "features.xml")
        root-deps (get-root-deps pom-xml)
        all-deps (reduce into [(get-deps pom-xml)
                               (get-descriptors pom-xml)
                               (get-features features-xml)])]
    (set/difference all-deps root-deps)))

(defn -lint [project-directory]
  (let [missing (main project-directory)]
    (if-not (empty? missing)
      (throw (MojoExecutionException.
               (str "Found missing dependencies in "
                    project-directory
                    "\n"
                    (with-out-str (pprint missing))))))))

(comment
  ; run _somewhere_ and _somehow_ get all feature files (being able to distinguish between ours and 3rd party)
  ; find all defined features in that set of files
  ; ensure unique names for each
  ; for those features files that are _ours_, ensure all have defined prefix

  ; run at prepare-package phase?
  ; distribution/ddf/target/dependencies/apache-karaf-XXX/system  xml files with the word 'feature'?
  (def data (xml/parse (.getPath (clojure.java.io/resource "features/src/main/resources/features.xml")))))



