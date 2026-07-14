package com.minimarket.minimarket.config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class FechaUtil {

    public final static String DATEFORMAT = "dd/MM/yyyy";
    public final static String DATE_JSON_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_REGEXP = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9]";

    public static String dateToString(Date fecha){
        // Convertir fecha a String con formato
        Date fechaOriginal = fecha;
        var localDateTime = fechaOriginal.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_JSON_FORMAT);
        String fechaFormateada = localDateTime.format(formatter);
        return fechaFormateada;

    }

}
