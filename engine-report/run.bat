set CLASSPATH=lib/*
java local.Main "jdbc:teiid:VDB@mm://HOST:PORT" "USERNAME" "PASSWORD" "select * from sys.tables"
