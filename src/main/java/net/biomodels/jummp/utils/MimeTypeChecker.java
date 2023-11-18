package net.biomodels.jummp.utils;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URLConnection;

public class MimeTypeChecker {
    //private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeChecker.class);
    public static String check(final File file) {
        String mime;
        try {
            Proxy proxy = ProxySetting.detect();
            URLConnection connection;
            if (proxy != null) {
                connection = file.toURI().toURL().openConnection(proxy);
            } else {
                connection = file.toURI().toURL().openConnection();
            }
            mime = connection.getContentType();
        } catch (IOException e) {
//            LOGGER.debug(String.valueOf(e), "could not get mime from file " + file);
            System.out.println("could not get mime from file " + file.getAbsolutePath());
            return null;
        }
        return mime;
    }
}
