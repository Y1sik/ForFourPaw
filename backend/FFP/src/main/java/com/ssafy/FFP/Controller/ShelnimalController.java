package com.ssafy.FFP.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.ssafy.FFP.Dto.CountingDto;
import com.ssafy.FFP.Service.DatasetService;
import org.checkerframework.checker.units.qual.A;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.FFP.Dto.CountingDto;
import com.ssafy.FFP.Dto.DatasetDto;
import com.ssafy.FFP.Dto.SearchDto;
import com.ssafy.FFP.Dto.ShelnimalDto;
import com.ssafy.FFP.Dto.ViewStoreDto;
import com.ssafy.FFP.Service.ShelnimalService;

@CrossOrigin(origins = {"http://localhost:5500", "https://j6e105.p.ssafy.io"}, allowCredentials = "true", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.HEAD,
        RequestMethod.OPTIONS })
@RestController
public class ShelnimalController {

    @Autowired
    ShelnimalService shelnimalService;

    @Autowired
    DatasetService datasetService;
    
    private static final int SUCCESS = 1;
    private static final int FAIL = -1;

    // ?????? ?????? ??????
    @GetMapping("/shel/detail/{no}")
    public ResponseEntity<?> select(@PathVariable String no){
//        int shelNo = Integer.parseInt(no);
        ShelnimalDto shelnimalDto = shelnimalService.select(no);

        if(shelnimalDto != null) {
            return ResponseEntity.ok().body(shelnimalDto);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
        }
    }

    // ?????? ?????? ??????
    @GetMapping("/shel/detail/notlogin/{no}")
    public ResponseEntity<?> selectByNotLogin(@PathVariable String no){
//        int shelNo = Integer.parseInt(no);
        ShelnimalDto shelnimalDto = shelnimalService.select(no);

        if(shelnimalDto != null) {
            return ResponseEntity.ok().body(shelnimalDto);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
        }
    }

    // ????????? ???????????? ?????? ????????? ?????? ?????? ??????
    @GetMapping("/shel/{offset}")
    public ResponseEntity<?> list(@PathVariable String offset) throws IOException{
        LocalDate seoulNow = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate seoulMonth = seoulNow.plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formatedNow = seoulNow.format(formatter);
        String formatedMonth = seoulMonth.format(formatter);
//        int sdt = Integer.parseInt(formatedNow);
//        int os = Integer.parseInt(offset);
//        List<ShelnimalDto> shelnimalDtos = shelnimalService.list(sdt, os, 9);
//        List<ShelnimalDto> count = shelnimalService.list(sdt, os, 100000);
//        CountingDto countingDto = new CountingDto(count.size(), shelnimalDtos, null);

        int max = 1;
        int result = 0;
        int totalCount = 0;
        List<ShelnimalDto> list = new ArrayList<>();

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1543061/abandonmentPublicSrvc/abandonmentPublic"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=D21SdqOm7AojlfaqJFBST3lCQ27QgAqg0TW%2BP%2BDSCI3ZZghj9ZwBWiAh7StgwiTR8WwHIbhtYIto%2F8StRy4qVw%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("endde","UTF-8") + "=" + URLEncoder.encode(formatedMonth, "UTF-8")); /*????????????(?????? ?????????) (YYYYMMDD)*/
        urlBuilder.append("&" + URLEncoder.encode("state","UTF-8") + "=" + URLEncoder.encode("protect", "UTF-8")); /*??????(?????? : null(??????), ????????? : notice, ????????? : protect)*/
        if(offset != null) urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(offset, "UTF-8")); /*????????? ?????? (????????? : 1)*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("9", "UTF-8")); /*???????????? ????????? ?????? (1,000 ??????), ????????? : 10*/
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*xml(?????????) ?????? json*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        String line;
        String str = "";
        while ((line = rd.readLine()) != null) {
            str += line + "\n";
        }
        try {
            if(str.contains("SERVICE ERROR"))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
            JSONParser jsonParse = new JSONParser(); // JSONParse??? json???????????? ?????? ????????? ?????? JSONObject??? ????????????.
            JSONObject jsonObject = (JSONObject) jsonParse.parse(str);
            JSONObject response = (JSONObject) jsonObject.get("response");
            JSONObject body = (JSONObject) response.get("body");
            JSONObject items = (JSONObject) body.get("items");
            totalCount = Integer.parseInt(body.get("totalCount").toString());
            if (max == 1)
                max = totalCount % 1000 == 0 ? totalCount / 1000 : totalCount / 1000 + 1;
            JSONArray item = (JSONArray) items.get("item"); // JSON ?????? ??????????????? tracks????????? ????????? ?????????
            if(item != null) {
                for (int i = 0; i < item.size(); i++) {
                    ShelnimalDto dto = new ShelnimalDto();
                    JSONObject itembody = (JSONObject) item.get(i);
                    dto.setDesertionNo(itembody.get("desertionNo") != null ? itembody.get("desertionNo").toString() : "-");
                    dto.setFilename(itembody.get("filename") != null ? itembody.get("filename").toString() : "-");
                    dto.setHappenDt(itembody.get("happenDt") != null ? itembody.get("happenDt").toString() : "-");
                    dto.setHappenPlace(itembody.get("happenPlace") != null ? itembody.get("happenPlace").toString() : "-");
                    dto.setKindCd(itembody.get("kindCd") != null ? itembody.get("kindCd").toString() : "-");
                    dto.setColorCd(itembody.get("colorCd") != null ? itembody.get("colorCd").toString() : "-");
                    dto.setAge(itembody.get("age") != null ? itembody.get("age").toString() : "-");
                    dto.setWeight(itembody.get("weight") != null ? itembody.get("weight").toString() : "-");
                    dto.setNoticeNo(itembody.get("noticeNo") != null ? itembody.get("noticeNo").toString() : "-");
                    dto.setNoticeSdt(itembody.get("noticeSdt") != null ? itembody.get("noticeSdt").toString() : "-");
                    dto.setNoticeEdt(itembody.get("noticeEdt") != null ? itembody.get("noticeEdt").toString() : "-");
                    dto.setPopfile(itembody.get("popfile") != null ? itembody.get("popfile").toString() : "-");
                    dto.setProcessState(itembody.get("processState") != null ? itembody.get("processState").toString() : "-");
                    dto.setSexCd(itembody.get("sexCd") != null ? itembody.get("sexCd").toString() : "-");
                    dto.setNeuterYn(itembody.get("neuterYn") != null ? itembody.get("neuterYn").toString() : "-");
                    dto.setSpecialMark(itembody.get("specialMark") != null ? itembody.get("specialMark").toString() : "-");
                    dto.setCareNm(itembody.get("careNm") != null ? itembody.get("careNm").toString() : "-");
                    dto.setCareTel(itembody.get("careTel") != null ? itembody.get("careTel").toString() : "-");
                    dto.setCareAddr(itembody.get("careAddr") != null ? itembody.get("careAddr").toString() : "-");
                    dto.setOrgNm(itembody.get("orgNm") != null ? itembody.get("orgNm").toString() : "-");
                    dto.setChargeNm(itembody.get("chargeNm") != null ? itembody.get("chargeNm").toString() : "-");
                    dto.setOfficeTel(itembody.get("officeTel") != null ? itembody.get("officetel").toString() : "-");
                    ShelnimalDto raw = shelnimalService.select(dto.getDesertionNo());
                    if(raw == null){
                        shelnimalService.insert(dto);
                    }
                    list.add(dto);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        rd.close();
        conn.disconnect();
        CountingDto countingDto = new CountingDto(totalCount, list, null);
        if(list != null) {
            return ResponseEntity.ok().body(countingDto);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
        }
    }

    // ?????? ?????? ??????
    @GetMapping("/shel/match/{no}")
    public ResponseEntity<?> match(@PathVariable String no){
        int userNo = Integer.parseInt(no);
        List<ShelnimalDto> shelnimalDtos = shelnimalService.match(userNo);

        if(shelnimalDtos != null) {
            return ResponseEntity.ok().body(shelnimalDtos);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
        }
    }

//    // ??????
//    @PostMapping("/shel")
//    public ResponseEntity<?> find(@RequestBody SearchDto searchDto){
//
//        searchDto.setLimit(9);
//        List<ShelnimalDto> shelnimalDtos = shelnimalService.find(searchDto);
//        searchDto.setLimit(100000);
//        List<ShelnimalDto> count = shelnimalService.find(searchDto);
//        CountingDto countingDto = new CountingDto(count.size(), shelnimalDtos, null);
//
//        if(shelnimalDtos != null) {
//            return ResponseEntity.ok().body(countingDto);
//        }
//        else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
//        }
//    }

    // ??????
    @PostMapping("/shel")
    public ResponseEntity<?> find(@RequestBody SearchDto searchDto) throws IOException {

        String offset = String.valueOf(searchDto.getOffset());
        String nyn = searchDto.getNeuterYn();
        String processState = searchDto.getProcessState();
        String kind = searchDto.getKindCd();
        String place = searchDto.getCareAddr();
        String upKindCd = searchDto.getUpKindCd();
        String noticeSdt = searchDto.getNoticeSdt();
        String noticeEdt = searchDto.getNoticeEdt();

        System.out.println(searchDto.toString());

        int max = 1;
        int result = 0;
        int totalCount = 0;
        List<ShelnimalDto> list = new ArrayList<>();

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1543061/abandonmentPublicSrvc/abandonmentPublic"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=D21SdqOm7AojlfaqJFBST3lCQ27QgAqg0TW%2BP%2BDSCI3ZZghj9ZwBWiAh7StgwiTR8WwHIbhtYIto%2F8StRy4qVw%3D%3D"); /*Service Key*/
        if(noticeSdt != null) urlBuilder.append("&" + URLEncoder.encode("bgnde","UTF-8") + "=" + URLEncoder.encode(noticeSdt, "UTF-8")); /*????????????(?????? ?????????) (YYYYMMDD)*/
        if(noticeEdt != null) urlBuilder.append("&" + URLEncoder.encode("endde","UTF-8") + "=" + URLEncoder.encode(noticeEdt, "UTF-8")); /*????????????(?????? ?????????) (YYYYMMDD)*/
        //urlBuilder.append("&" + URLEncoder.encode("care_reg_no","UTF-8") + "=" + URLEncoder.encode(" ", "UTF-8")); /*??????????????? (????????? ?????? OPEN API ??????)*/
        // urlBuilder.append("&" + URLEncoder.encode("org_cd","UTF-8") + "=" + URLEncoder.encode(" ", "UTF-8")); /*??????????????? (????????? ?????? OPEN API ??????)*/
        if(upKindCd != null) urlBuilder.append("&" + URLEncoder.encode("upkind","UTF-8") + "=" + URLEncoder.encode(upKindCd, "UTF-8")); /*???????????? (??? : 417000, ????????? : 422400, ?????? : 429900)*/
        if(kind != null) urlBuilder.append("&" + URLEncoder.encode("kind","UTF-8") + "=" + URLEncoder.encode(kind, "UTF-8")); /*???????????? (?????? ?????? OPEN API ??????)*/
        if(place != null) urlBuilder.append("&" + URLEncoder.encode("upr_cd","UTF-8") + "=" + URLEncoder.encode(place, "UTF-8")); /*???????????? (?????? ?????? OPEN API ??????)*/
        if(processState != null) urlBuilder.append("&" + URLEncoder.encode("state","UTF-8") + "=" + URLEncoder.encode(processState, "UTF-8")); /*??????(?????? : null(??????), ????????? : notice, ????????? : protect)*/
        if(nyn != null) urlBuilder.append("&" + URLEncoder.encode("neuter_yn","UTF-8") + "=" + URLEncoder.encode(nyn, "UTF-8")); /*?????? (?????? : null(??????), ??? : Y, ????????? : N, ?????? : U)*/
        if(offset != null) urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(offset, "UTF-8")); /*????????? ?????? (????????? : 1)*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("9", "UTF-8")); /*???????????? ????????? ?????? (1,000 ??????), ????????? : 10*/
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*xml(?????????) ?????? json*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        String line;
        String str = "";
        while ((line = rd.readLine()) != null) {
            str += line + "\n";
        }
        try {
            if(str.contains("SERVICE ERROR"))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
            JSONParser jsonParse = new JSONParser(); // JSONParse??? json???????????? ?????? ????????? ?????? JSONObject??? ????????????.
            JSONObject jsonObject = (JSONObject) jsonParse.parse(str);
            JSONObject response = (JSONObject) jsonObject.get("response");
            JSONObject body = (JSONObject) response.get("body");
            JSONObject items = (JSONObject) body.get("items");
            totalCount = Integer.parseInt(body.get("totalCount").toString());
            if (max == 1)
                max = totalCount % 1000 == 0 ? totalCount / 1000 : totalCount / 1000 + 1;
            JSONArray item = (JSONArray) items.get("item"); // JSON ?????? ??????????????? tracks????????? ????????? ?????????
            if(item != null) {
                for (int i = 0; i < item.size(); i++) {
                    ShelnimalDto dto = new ShelnimalDto();
                    JSONObject itembody = (JSONObject) item.get(i);
                    dto.setDesertionNo(itembody.get("desertionNo") != null ? itembody.get("desertionNo").toString() : "-");
                    dto.setFilename(itembody.get("filename") != null ? itembody.get("filename").toString() : "-");
                    dto.setHappenDt(itembody.get("happenDt") != null ? itembody.get("happenDt").toString() : "-");
                    dto.setHappenPlace(itembody.get("happenPlace") != null ? itembody.get("happenPlace").toString() : "-");
                    dto.setKindCd(itembody.get("kindCd") != null ? itembody.get("kindCd").toString() : "-");
                    dto.setColorCd(itembody.get("colorCd") != null ? itembody.get("colorCd").toString() : "-");
                    dto.setAge(itembody.get("age") != null ? itembody.get("age").toString() : "-");
                    dto.setWeight(itembody.get("weight") != null ? itembody.get("weight").toString() : "-");
                    dto.setNoticeNo(itembody.get("noticeNo") != null ? itembody.get("noticeNo").toString() : "-");
                    dto.setNoticeSdt(itembody.get("noticeSdt") != null ? itembody.get("noticeSdt").toString() : "-");
                    dto.setNoticeEdt(itembody.get("noticeEdt") != null ? itembody.get("noticeEdt").toString() : "-");
                    dto.setPopfile(itembody.get("popfile") != null ? itembody.get("popfile").toString() : "-");
                    dto.setProcessState(itembody.get("processState") != null ? itembody.get("processState").toString() : "-");
                    dto.setSexCd(itembody.get("sexCd") != null ? itembody.get("sexCd").toString() : "-");
                    dto.setNeuterYn(itembody.get("neuterYn") != null ? itembody.get("neuterYn").toString() : "-");
                    dto.setSpecialMark(itembody.get("specialMark") != null ? itembody.get("specialMark").toString() : "-");
                    dto.setCareNm(itembody.get("careNm") != null ? itembody.get("careNm").toString() : "-");
                    dto.setCareTel(itembody.get("careTel") != null ? itembody.get("careTel").toString() : "-");
                    dto.setCareAddr(itembody.get("careAddr") != null ? itembody.get("careAddr").toString() : "-");
                    dto.setOrgNm(itembody.get("orgNm") != null ? itembody.get("orgNm").toString() : "-");
                    dto.setChargeNm(itembody.get("chargeNm") != null ? itembody.get("chargeNm").toString() : "-");
                    dto.setOfficeTel(itembody.get("officeTel") != null ? itembody.get("officetel").toString() : "-");
                    ShelnimalDto raw = shelnimalService.select(dto.getDesertionNo());
                    if(raw == null){
                        shelnimalService.insert(dto);
                    }
                    list.add(dto);
                }
            }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println(result);
            rd.close();
            conn.disconnect();

            CountingDto countingDto = new CountingDto(totalCount, list, null);
            if(list != null) {
                return ResponseEntity.ok().body(countingDto);
            }
            else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
            }
    }
    
    // ??????????????? ??????????????? ???????????? ??????
    @GetMapping("/shel/view/notlogin")
    public ResponseEntity<?> mainList(){
    	System.out.println("mainList : ????????????");
        List<DatasetDto> shelnimalDtos = shelnimalService.mainList();

        if(shelnimalDtos != null) {
            return ResponseEntity.ok().body(shelnimalDtos);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
        }
    }
    
    // ???????????? ??????????????? ???????????? ??????
    @GetMapping("/authorization/shel/view/login/{no}")
    public ResponseEntity<?> mainListLogin(@PathVariable String no){
    	System.out.println("mainList : ????????? " + no);
//        List<DatasetDto> shelnimalDtos = shelnimalService.mainList();
        
        List<DatasetDto> shelnimalDtos = shelnimalService.mainListLogin(no);

        if(shelnimalDtos != null) {
            return ResponseEntity.ok().body(shelnimalDtos);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
        }
    }
    
    // ?????? ??????
    @GetMapping("/shel/search/list/{kind}")
    public ResponseEntity<?> searchList(@PathVariable String kind){
    	System.out.println("searchList " + kind);
        List<String> kinds = shelnimalService.searchList(kind);

        if(kinds != null) {
            return ResponseEntity.ok().body(kinds);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "?????? ??????.");
        }
    }
    
    // ??????
    @PostMapping("/shel/view/detail")
    public ResponseEntity<?> viewStore(@RequestBody ViewStoreDto viewStoreDto){
    	
        if (shelnimalService.viewStore(viewStoreDto) == SUCCESS) {
            System.out.println("?????? ?????? ??????");
            System.out.println(viewStoreDto);
            return new ResponseEntity<Integer>(SUCCESS, HttpStatus.OK);
        } else {
            System.out.println("?????? ?????? ??????");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "??????");
        }
    }
}
