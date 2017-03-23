(ns comic-reader.database)

(defprotocol Database
  (connection [database] "Returns a connection to the database.")
  (destroy [database] "Destroy the database."))
