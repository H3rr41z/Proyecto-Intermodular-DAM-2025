package com.renaix.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utilidades para manejo de fechas
 */
object DateUtils {

    private val locale = Locale("es", "ES")

    // Formatos de fecha
    private val fullDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", locale)
    private val timeFormat = SimpleDateFormat("HH:mm", locale)
    private val dayMonthFormat = SimpleDateFormat("dd MMM", locale)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    /**
     * Convierte un timestamp ISO 8601 a Date
     */
    fun parseIsoDate(isoString: String): Date? {
        return try {
            isoFormat.parse(isoString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convierte una Date a string en formato completo
     */
    fun formatFullDate(date: Date): String {
        return fullDateFormat.format(date)
    }

    /**
     * Convierte una Date a string en formato corto
     */
    fun formatShortDate(date: Date): String {
        return shortDateFormat.format(date)
    }

    /**
     * Convierte una Date a string solo con la hora
     */
    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }

    /**
     * Convierte una Date a formato "dd MMM" (ej: "15 Ene")
     */
    fun formatDayMonth(date: Date): String {
        return dayMonthFormat.format(date)
    }

    /**
     * Convierte un timestamp ISO a formato relativo
     * Ej: "Hace 2 horas", "Ayer", "15 Ene"
     */
    fun formatRelativeTime(isoString: String): String {
        val date = parseIsoDate(isoString) ?: return "Fecha desconocida"
        return formatRelativeTime(date)
    }

    /**
     * Convierte una Date a formato relativo
     */
    fun formatRelativeTime(date: Date): String {
        val now = Date()
        val diffMillis = now.time - date.time

        return when {
            diffMillis < 0 -> formatShortDate(date) // Fecha futura

            // Menos de 1 minuto
            diffMillis < TimeUnit.MINUTES.toMillis(1) -> "Justo ahora"

            // Menos de 1 hora
            diffMillis < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
                "Hace $minutes min"
            }

            // Menos de 24 horas
            diffMillis < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                if (hours == 1L) "Hace 1 hora" else "Hace $hours horas"
            }

            // Ayer
            isYesterday(date) -> "Ayer ${formatTime(date)}"

            // Esta semana
            isThisWeek(date) -> "${getDayOfWeek(date)} ${formatTime(date)}"

            // Este año
            isThisYear(date) -> formatDayMonth(date)

            // Años anteriores
            else -> formatShortDate(date)
        }
    }

    /**
     * Verifica si una fecha es de ayer
     */
    private fun isYesterday(date: Date): Boolean {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val dateCalendar = Calendar.getInstance().apply {
            time = date
        }

        return yesterday.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Verifica si una fecha es de esta semana
     */
    private fun isThisWeek(date: Date): Boolean {
        val now = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply {
            time = date
        }

        return now.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                now.get(Calendar.WEEK_OF_YEAR) == dateCalendar.get(Calendar.WEEK_OF_YEAR)
    }

    /**
     * Verifica si una fecha es de este año
     */
    private fun isThisYear(date: Date): Boolean {
        val now = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply {
            time = date
        }

        return now.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR)
    }

    /**
     * Obtiene el día de la semana en español
     */
    private fun getDayOfWeek(date: Date): String {
        val calendar = Calendar.getInstance().apply {
            time = date
        }

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> ""
        }
    }

    /**
     * Calcula la diferencia entre dos fechas en días
     */
    fun getDaysDifference(date1: Date, date2: Date): Long {
        val diffMillis = kotlin.math.abs(date1.time - date2.time)
        return TimeUnit.MILLISECONDS.toDays(diffMillis)
    }

    /**
     * Verifica si una fecha es hoy
     */
    fun isToday(date: Date): Boolean {
        val now = Calendar.getInstance()
        val dateCalendar = Calendar.getInstance().apply {
            time = date
        }

        return now.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Obtiene la fecha actual
     */
    fun now(): Date {
        return Date()
    }

    /**
     * Añade días a una fecha
     */
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, days)
        }
        return calendar.time
    }

    /**
     * Añade horas a una fecha
     */
    fun addHours(date: Date, hours: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.HOUR_OF_DAY, hours)
        }
        return calendar.time
    }

    /**
     * Formatea una fecha para mostrar en chat
     * - Si es hoy: solo hora
     * - Si es esta semana: día de la semana
     * - Si es este año: día y mes
     * - Años anteriores: fecha completa
     */
    fun formatChatDate(date: Date): String {
        return when {
            isToday(date) -> formatTime(date)
            isThisWeek(date) -> getDayOfWeek(date)
            isThisYear(date) -> formatDayMonth(date)
            else -> formatShortDate(date)
        }
    }
}
