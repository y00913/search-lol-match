package com.example.lol.controller;

import com.example.lol.dto.LeagueEntryDTO;
import com.example.lol.dto.MatchDTO;
import com.example.lol.dto.SummonerDTO;
import com.example.lol.service.IconService;
import com.example.lol.service.SummonerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private SummonerService summonerService;

    @GetMapping
    public String index(){

        return "index";
    }

    @GetMapping("/search")
    public String getResult(String summonerName, Model model){
        String summonerNameRepl = summonerName.replaceAll(" ","%20");
        SummonerDTO summonerDTO = summonerService.callRiotAPISummonerByName(summonerNameRepl);

        if(summonerDTO.getName() != null) {
            model.addAttribute("summoner",summonerDTO);
            LeagueEntryDTO leagueEntry = summonerService.callLeagueEntry(summonerDTO.getId());
            model.addAttribute("leagueEntry", leagueEntry);
        }

        System.out.println(summonerDTO.getName() + "   puuid : " + summonerDTO.getPuuid() + "   id : " + summonerDTO.getId());

        return "result";
    }

    @GetMapping("/{summonerName}/{start}")
    public String callMatchTable(@PathVariable String summonerName, @PathVariable int start, Model model) {
        String summonerNameRepl = summonerName.replaceAll(" ","%20");
        SummonerDTO summonerDTO = summonerService.callRiotAPISummonerByName(summonerNameRepl);

        model.addAttribute("result",summonerDTO);

        if(summonerDTO != null) {
            List<String> matchHistory = summonerService.callMatchHistory(summonerDTO.getPuuid(), start  );
            List<MatchDTO> matchDTOs = summonerService.callMatchAbout(matchHistory,summonerDTO.getPuuid());

            model.addAttribute("matches",matchDTOs);
        }

        return "table :: body";
    }

    @GetMapping("/detail/{matchId}")
    public String callDeatilMatchTable(@PathVariable String matchId, Model model) {
        MatchDTO matchDTO = summonerService.callDetailMatch(matchId);

        model.addAttribute("match",matchDTO);

        return "detail-table :: body";
    }

}
