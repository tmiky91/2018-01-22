package it.polito.tdp.seriea.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.seriea.model.Match;
import it.polito.tdp.seriea.model.Season;
import it.polito.tdp.seriea.model.Team;

public class SerieADAO {

	public List<Season> listAllSeasons() {
		String sql = "SELECT season, description FROM seasons";
		List<Season> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Season(res.getInt("season"), res.getString("description")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public List<Team> listTeams() {
		String sql = "SELECT team FROM teams";
		List<Team> result = new ArrayList<>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Team(res.getString("team")));
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Match> listMatchesForTeam(Team squadra, Map<Integer, Season> stagioniIdMap, Map<String, Team> squadreIdMap){
		final String sql=	"select m.match_id as id, m.Season as season, m.`Div`, m.Date, m.HomeTeam as homet, m.AwayTeam as awayt, m.FTHG, m.FTAG, m.FTR " + 
							"from matches as m " + 
							"where m.HomeTeam =? or m.AwayTeam =?";
		List<Match> result = new LinkedList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, squadra.getTeam());
			st.setString(2, squadra.getTeam());
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Match match = new Match( res.getInt("id"), stagioniIdMap.get(res.getInt("season")), res.getString("m.Div"), res.getDate("m.Date").toLocalDate(), squadreIdMap.get(res.getString("homet")), squadreIdMap.get(res.getString("awayt")), res.getInt("m.FTHG"), res.getInt("m.FTAG"), res.getString("m.FTR"));
				result.add(match);
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}

