package net.example.apstudent.roboticsexplorer.util

/**
 * Data class for all things FRC Team
 *
 * This is not an extensive list of all the possible data points that can be extracted from TBA,
 * just what I needed to complete this app.  I just add things as I go
 *
 * This particular file is written in kotlin because even though I am not confident enough to use
 * it for the whole app, kotlin data classes are orders of magnitude easier to implement than doing
 * the same thing in java.
 **/
data class TeamData(var teamKey: String = "",
                    var teamName: String = "",
                    var teamNumber: String = "",
                    var rank: String = "",
                    var status: String = "",
                    var qualWins: String = "",
                    var qualLosses: String = "",
                    var qualTies: String = "",
                    var qualDq: String = "",
                    var elimWins: String = "",
                    var elimLosses: String = "",
                    var elimTies: String = "",
                    var elimStatus: String = "")
