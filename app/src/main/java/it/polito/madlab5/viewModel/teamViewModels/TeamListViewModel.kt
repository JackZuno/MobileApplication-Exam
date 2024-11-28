package it.polito.madlab5.viewModel.TeamViewModels

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import it.polito.madlab5.model.Team.Team


class TeamListViewModel(
    newTeamList: MutableList<Team> = mutableListOf()
) {

    var teamList = newTeamList.toMutableStateList()
    //var appMembersProfile: MutableList<Profile> = mutableListOf()

    fun addTeam(team: Team){
        teamList.add(team)
    }

    fun getTeamByIndex(index: Int): Team? {
        return if (index in 0 until teamList.size) {
            teamList[index]
        } else {
            null
        }
    }

    fun getTeamById(id: String): Team {
        return teamList.first {
            Log.d("teamID",id)
            Log.d("TEAM", "TeamName: ${it.name}, teamId: ${it.teamID}")
            it.teamID == id
        }
    }

    fun getTeamByIdDeepLink(id: String): Team? {
        return teamList.firstOrNull {
            Log.d("teamID",id)
            Log.d("TEAM", "TeamName: ${it.name}, teamId: ${it.teamID}")
            it.teamID == id
        }
    }

    fun getIndexById(id: String): Int {
        return teamList.indexOfFirst {
            Log.d("teamID in the search", id)
            it.teamID == id
        }
    }

}