package it.polito.tdp.seriea.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.seriea.db.SerieADAO;

public class Model {
	
	private List<Team> squadre;
	private List<Season> stagioni;
	private Team squadraSelezionata;
	private Map<Season, Integer> punteggi;
	private Map<Integer, Season> stagioniIdMap;
	private Map<String, Team> squadreIdMap;
	private List<Season> stagioniConsecutive;
	private Graph<Season, DefaultWeightedEdge> grafo;
	private List<Season> percorsoBest;
	
	public Model() {
		SerieADAO dao = new SerieADAO();
		this.squadre=dao.listTeams();
		this.stagioni=dao.listAllSeasons();
		this.stagioniIdMap = new HashMap<>();
		this.squadreIdMap = new HashMap<>();
		for(Season s: stagioni) {
			this.stagioniIdMap.put(s.getSeason(), s);
		}
		for(Team t: squadre) {
			this.squadreIdMap.put(t.getTeam(), t);
		}
	}
	
	public List<Team> getSquadre(){
		return this.squadre;
	}
	
	public Map<Season, Integer> calcolaPunteggi(Team squadra){
		this.squadraSelezionata=squadra;
		this.punteggi = new HashMap<Season, Integer>();
		SerieADAO dao = new SerieADAO();
		List<Match> partite = dao.listMatchesForTeam(squadra, stagioniIdMap, squadreIdMap);
		for(Match m: partite) {
			Season stagione = m.getSeason();
			int punti =0;
			if(m.getFtr().equals("D")) {
				punti = 1;
			}else {
				if((m.getHomeTeam().equals(squadra) && m.getFtr().equals("H")) || 
						(m.getAwayTeam().equals(squadra) && m.getFtr().equals("A"))) {
					punti = 3;
				}
			}
			Integer attuale = punteggi.get(stagione);
			if(attuale==null) {
				attuale=0;
			}
			punteggi.put(stagione, attuale+punti);
		}
		return punteggi;
	}
	
	public Season calcolaAnnataDOro() {
		this.grafo = new SimpleDirectedWeightedGraph<Season, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(grafo, punteggi.keySet());
		for(Season s1: grafo.vertexSet()) {
			for(Season s2: grafo.vertexSet()) {
				if(!s1.equals(s2)) {
					int punti1 = punteggi.get(s1);
					int punti2 = punteggi.get(s2);
					if(punti1 > punti2) {
						Graphs.addEdge(grafo, s2, s1, punti1-punti2);
					}else {
						Graphs.addEdge(grafo, s1, s2, punti2-punti1);
					}
				}
			}
		}
		Season migliore = null;
		int max =0;
		for(Season s: grafo.vertexSet()) {
			int valore = pesoStagione(s);
			if(valore>max) {
				max=valore;
				migliore=s;
			}
		}
		return migliore;
	}
	
	private int pesoStagione(Season s) {
		int somma=0;
		for(DefaultWeightedEdge edge :grafo.incomingEdgesOf(s)) {
			somma+=grafo.getEdgeWeight(edge);
		}
		for(DefaultWeightedEdge edge: grafo.outgoingEdgesOf(s)) {
			somma-=grafo.getEdgeWeight(edge);
		}
		return somma;
	}
	
	public List<Season> camminoVirtuoso() {
		//trova le stagioni consecutive
		stagioniConsecutive = new ArrayList<Season>(punteggi.keySet());
		Collections.sort(stagioniConsecutive);
		List<Season> parziale= new ArrayList<>();
		percorsoBest = new ArrayList<>();
		for(Season s: grafo.vertexSet()) {
			parziale.add(s);
			cerca(1, parziale);
			parziale.remove(0);
		}
		return percorsoBest;
		
	}
	
	/*
	 * RICORSIONE
	 * Soluzione parziale: lista di Season (lista di vertici)
	 * Livello ricorsione: lunghezza della lista
	 * Casi terminali: non trova altri vertici da aggiungere -> verifica se il cammino a lunghezza massima fra quelli visti fin'ora
	 * Generazione delle soluzioni: vertici connessi all'ultimo vertice del percorso(con arco orientato nel verso giusto), non ancora parte del percorso, relativi a stagioni consecutive 
	 */
	
	private void cerca(int livello, List<Season> parziale) {
		boolean trovato=false;
		//genera nuove soluzioni
		Season ultimo=parziale.get(livello-1);
		for(Season prossimo: Graphs.successorListOf(grafo, ultimo)) {
			if(!parziale.contains(prossimo)) {
				if(stagioniConsecutive.indexOf(ultimo)+1 == stagioniConsecutive.lastIndexOf(prossimo)) {
					//candidato accettabile -> fai ricorsione
					trovato=true;
					parziale.add(prossimo);
					cerca(livello+1, parziale);
					parziale.remove(livello);
				}
			}
		}
		//valuta caso terminale
		if(!trovato) {
			if(parziale.size()>percorsoBest.size()) {
				percorsoBest = new ArrayList<Season>(parziale); //clona il best
			}
		}
	}

}
