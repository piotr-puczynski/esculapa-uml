# Introduction #

If you want to see details about how the model is checked and executed in EsculapaUML it is possible to enable advanced debugging mode.


# Details #

To enable debugging we need to find plugin file `dk.dtu.imm.esculapauml.core.jar` in _plugins_ directory and open it as archive in ZIP editor:
  * Find configuration file `logging.properties` in this archive
  * In configuration file uncomment line:
`log4j.logger.dk.dtu.imm.esculapauml=DEBUG, console, file`
  * If you want to see only important messages you may change logging level to `INFO` instead of `DEBUG`.
  * Save the file and place it back in archive.


# More Info #
The logging in EsculapaUML is provided by **log4j** framework. You can find more information here: http://logging.apache.org/log4j/1.2/index.html