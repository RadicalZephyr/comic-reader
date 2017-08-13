(ns comic-reader.database)

(defprotocol Database
  (destroy [database] "Destroy the database."))
