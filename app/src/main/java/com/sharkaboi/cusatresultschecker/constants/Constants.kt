package com.sharkaboi.cusatresultschecker.constants

object Constants {
    const val NOTIFICATION_CHANNEL_DESC: String = "Used to notify results from cusat website"
    const val NOTIFICATION_CHANNEL_NAME: String = "Cusat Results Notification Channel"
    const val NOTIFICATION_CHANNEL_ID: String = "cusatresultschecker-notif"

    const val userAgents: String =
        "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"
    const val workerTag: String = "cusatResultsWorker"

    const val cusatResultsSite = "https://results.cusat.ac.in/regforms/mrklst-main.php"

    const val REG_NO_KEY = "rno"

    const val EXAM_KEY = "exam"
    val examTypes = listOf(
        "Regular",
        "Supplimentary",
        "Revaluation"
    )

    const val SEM_KEY = "sem"
    val semesters = listOf(
        "I",
        "II",
        "III",
        "IV",
        "V",
        "VI",
        "VII",
        "VIII",
    )

    const val SCHEME_KEY = "scheme"
    val schemes = listOf(
        "2015",
        "2006",
    )

    // This can change in the future, better to scrap and display
    // as some keys are not following the pattern of "MONTH YEAR".
    // Can ask the user to enter, but not that reliable as the
    // site doesn't guarantee the key's pattern
    const val EDATE_KEY = "edate"
    val examDates = listOf(
        "APRIL 2022",
        "NOVEMBER 2021",
        "APRIL 2021",
        "NOVEMBER 2020",
        "APRIL 2020",
        "FEBRUARY 2020",
        "NOVEMBER 2019",
        "JULY 2019 ",
        "APRIL 2019",
        "NOVEMBER 2018",
        "JANUARY 2019",
        "AUGUST 2018",
        "APRIL 2018",
        "NOVEMBER 2017",
        "SEPTEMBER 2017",
        "JUNE 2017",
        "MAY 2017",
        "APRIL 2017",
        "MARCH 2017",
        "NOVEMBER 2016",
        "JANUARY 2017",
        "DECEMBER 2016",
        "JUNE 2016",
        "SEPTEMBER 2016",
        "APRIL 2016",
        "JULY 2016",
        "MAY 2016",
        "NOVEMBER 2015",
        "JANUARY 2016",
        "APRIL 2015",
        "SEPTEMBER 2015",
        "DECEMBER 2015",
        "MAY 2015",
        "NOVEMBER 2014",
        "SEPTEMBER 2014",
        "JUNE 2014",
        "AUGUST 2014",
        "APRIL 2014",
        "MAY 2014",
        "NOVEMBER 2013",
        "SEPTEMBER 2013",
        "JUNE 2013",
        "AUGUST 2013",
        "APRIL 2013",
        "FEBRUARY 2013",
        "NOVEMBER 2012",
        "AUGUST 2012",
        "JUNE 2012",
        "APRIL 2012",
        "FEBRUARY 2012",
        "SEPTEMBER 2011",
        "JUNE 2011",
        "APRIL 2011",
        "JANUARY 2011",
        "NOVEMBER 2010",
        "NOVEMBER 2011",
        "SEPTEMBER 2010",
        "JUNE 2010",
        "JULY 2010",
        "APRIL 2010",
        "NOVEMBER 2009",
        "NOVEMBER 2009",
        "OCTOBER 2009",
        "SEPTEMBER 2009",
        "JULY 2009",
        "JUNE 2009",
        "APRIL 2009",
        "NOVEMBER 2008",
        "NOVEMBER 2008",
        "JUNE 2008",
        "APRIL 2008",
        "NOVEMBER 2007",
        "SEPTEMBER 2008",
    )
}