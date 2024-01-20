package com.example.lol.controller;

import com.example.lol.dto.*;
import com.example.lol.repository.*;
import com.example.lol.service.IconService;
import com.example.lol.service.SummonerService;
import com.example.lol.util.SummonerNameParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private SummonerService summonerService;

    @Autowired
    private IconService iconService;

    @Autowired
    private SummonerRepository summonerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RankTypeRepository rankTypeRepository;

    @GetMapping
    public String index() {

        return "index";
    }

    @GetMapping("/search")
    public String getResult(String nameAndTag, Model model) {
        RiotInfo riotInfo = SummonerNameParse.getNameAndTage(nameAndTag);
        Optional<Summoner> summonerDTO = summonerRepository.findByNameAndTagLine(riotInfo.getName(), riotInfo.getTagLine());
        String puuid = "";

        if (summonerDTO.isPresent()) {
            model.addAttribute("summoner", summonerDTO.get());
            puuid = summonerDTO.get().getPuuid();
        } else {
            Summoner tmp = summonerService.callRiotAPISummonerByPuuid(riotInfo);
            if(tmp.getName() != null){
                puuid = tmp.getPuuid();
                model.addAttribute("summoner", tmp);
            }
        }

        if(matchRepository.findByPuuid(puuid).isEmpty()){
            model.addAttribute("check", false);
        } else {
            model.addAttribute("check", true);
        }

        System.out.println(summonerDTO);

        return "result";
    }

    @GetMapping("/{id}/{name}/{tagLine}/{summonerLevel}/{profileIcon}/{updateAt}")
    public String callLeagueInfo(@PathVariable String id,
                                 @PathVariable String name,
                                 @PathVariable String tagLine,
                                 @PathVariable String summonerLevel,
                                 @PathVariable String profileIcon,
                                 @PathVariable String updateAt,
                                 Model model){
        Optional<RankType> rankType = rankTypeRepository.findById(id);

        if(rankType.isPresent()){
            if(rankType.get().getFlexUserTier() != null){
                model.addAttribute("flexTierImg", iconService.callTierIcon(rankType.get().getFlexUserTier()));
            }
            else {
                model.addAttribute("flexTierImg", iconService.callTierIcon("unranked"));
            }
            if(rankType.get().getSoloUserTier() != null){
                model.addAttribute("soloTierImg", iconService.callTierIcon(rankType.get().getSoloUserTier()));
            }
            else {
                model.addAttribute("soloTierImg", iconService.callTierIcon("unranked"));
            }

            model.addAttribute("rankType", rankType.get());
        } else {
            RankType rankType1 = summonerService.callRankTier(id);

            if(rankType1.getFlexUserTier() != null){
                model.addAttribute("flexTierImg", iconService.callTierIcon(rankType1.getFlexUserTier()));
            }
            else {
                model.addAttribute("flexTierImg", iconService.callTierIcon("unranked"));
            }
            if(rankType1.getSoloUserTier() != null){
                model.addAttribute("soloTierImg", iconService.callTierIcon(rankType1.getSoloUserTier()));
            }
            else {
                model.addAttribute("soloTierImg", iconService.callTierIcon("unranked"));
            }

            model.addAttribute("rankType", rankType1);
        }

        model.addAttribute("name", name);
        model.addAttribute("tagLine", tagLine);
        model.addAttribute("summonerLevel", summonerLevel);
        model.addAttribute("profileIcon", iconService.callProfileIcon(profileIcon));

        Long diff = ChronoUnit.MINUTES.between(LocalDateTime.parse(updateAt), LocalDateTime.now());
        model.addAttribute("updateAt", diff);

        return "league-info";
    }

    @GetMapping("/{puuid}/{start}")
    public String callMatchTable(@PathVariable String puuid, @PathVariable int start, Model model) {
        Page<Match> matchDTOs = matchRepository.findByPuuid(PageRequest.of(start,10, Sort.Direction.DESC, "endTimeStamp"), puuid);
        for(Match match : matchDTOs){
            Match tmp = match;

            tmp.setItem0(iconService.callItemIcon(tmp.getItem0()));
            tmp.setItem1(iconService.callItemIcon(tmp.getItem1()));
            tmp.setItem2(iconService.callItemIcon(tmp.getItem2()));
            tmp.setItem3(iconService.callItemIcon(tmp.getItem3()));
            tmp.setItem4(iconService.callItemIcon(tmp.getItem4()));
            tmp.setItem5(iconService.callItemIcon(tmp.getItem5()));
            tmp.setItem6(iconService.callItemIcon(tmp.getItem6()));
            tmp.setChampionName(iconService.callChampionIcon(tmp.getChampionName()));
            tmp.setSpell1Id(iconService.callSpellIcon(tmp.getSpell1Id()));
            tmp.setSpell2Id(iconService.callSpellIcon(tmp.getSpell2Id()));
            tmp.setPrimaryPerk(iconService.callPrimaryPerkIcon(tmp.getPrimaryPerk()));
            tmp.setSubPerk(iconService.callSubPerkIcon(tmp.getSubPerk()));

            long timestamp = System.currentTimeMillis() / 1000 - tmp.getEndTimeStamp();
            String endTime;
            if (timestamp < 60) {
                endTime = Math.round(timestamp) + "초 전";
            } else if (timestamp / 60 < 60) {
                endTime = Math.round(timestamp / 60) + "분 전";
            } else if (timestamp / 60 / 60 < 24) {
                endTime = Math.round(timestamp / 60 / 60) + "시간 전";
            } else if (timestamp / 60 / 60 / 24 < 30) {
                endTime = Math.round(timestamp / 60 / 60 / 24) + "일 전";
            } else {
                endTime = Math.round(timestamp / 60 / 60 / 24 / 30) + "달 전";
            }
            tmp.setEndTime(endTime);
        }

        model.addAttribute("matches", matchDTOs.getContent());

        return "table :: body";
    }

    @GetMapping("/detail/{matchId}")
    public String callDeatilMatchTable(@PathVariable String matchId, Model model) {
        List<MatchUserInfo> matchUserInfoDTOs = summonerService.callDetailMatch(matchId);

        model.addAttribute("matchInfo", matchUserInfoDTOs);

        return "detail-table :: body";
    }

    @GetMapping("/riot.txt")
    public String callRiotText(Model model){
        return "riot.txt";
    }
}
