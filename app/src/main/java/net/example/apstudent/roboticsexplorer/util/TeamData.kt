/* Data class for FRC teams.
 *     Copyright (C) 2021  Nathan Jankowski
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
