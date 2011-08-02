package org.springsource.tutorial.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springsource.tutorial.domain.USState;

@RooWebScaffold(path = "usstates", formBackingObject = USState.class)
@RequestMapping("/usstates")
@Controller
public class USStateController {
    
    private static final String RANGE_PREFIX = "items=";
    
    private static final String CONTENT_RANGE_HEADER = "Content-Range";
    
    private static final String ACCEPT_JSON = "Accept=application/json";
    
    @RequestMapping(value="/{id}", method=RequestMethod.GET, headers=ACCEPT_JSON)
    public @ResponseBody USState getJson(@PathVariable("id") Long id) {
        return USState.findUSState(id);
    }
    
    /**
     * TODO - Should probably be stricter here in following the HTTP spec, 
     * i.e. returning the proper 20x response instead of an explicit redirect
     */
    @RequestMapping(method=RequestMethod.POST, headers=ACCEPT_JSON)
    public String createJson(@Valid @RequestBody USState state) {
        state.persist();
        return "redirect:/usstates/"+state.getId();
    }
    
    @RequestMapping(value="/{id}", method={RequestMethod.POST, RequestMethod.PUT}, headers={ACCEPT_JSON,"If-None-Match=*"})
    public String createJsonWithId(@Valid @RequestBody USState state, @PathVariable("id") Long id) {
        Assert.isTrue(USState.findUSState(id) == null);
        return updateJson(state, id);
    }
    
    @RequestMapping(value="/{id}", method={RequestMethod.POST, RequestMethod.PUT}, headers={ACCEPT_JSON,"If-Match=*"})
    public String overWriteJson(@Valid @RequestBody USState state, @PathVariable("id") Long id) {
        Assert.isTrue(USState.findUSState(id) != null);
        return updateJson(state, id);
    }
    
    /**
     * TODO - Should probably be stricter here in following the HTTP spec, 
     * i.e. returning the proper 20x response instead of an automatic redirect
     */
    @RequestMapping(value="/{id}", method=RequestMethod.PUT, headers=ACCEPT_JSON)
    public String updateJson(@Valid @RequestBody USState state, @PathVariable("id") Long id) {
        Assert.isTrue(id.equals(state.getId()));
        state.merge();
        return "redirect:/usstates/"+state.getId();
    }
    
    @RequestMapping(method=RequestMethod.DELETE, headers=ACCEPT_JSON) 
    public @ResponseBody ResponseEntity<String> deleteJson(@PathVariable("id") Long id) {
        USState.findUSState(id).remove();
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }
    
    @RequestMapping(method=RequestMethod.GET, headers=ACCEPT_JSON)
    public @ResponseBody HttpEntity<List<USState>> listJson() {
        HttpHeaders headers = new HttpHeaders();
        List<USState> body = null;
        body = USState.findAllUSStates();
        headers.add(CONTENT_RANGE_HEADER, getContentRangeValue(0, body.size(), new Integer(body.size()).longValue()));
        return new HttpEntity<List<USState>>(body, headers);
    }
    
    @RequestMapping(method=RequestMethod.GET, headers={ACCEPT_JSON, "Range"})
    public @ResponseBody HttpEntity<List<USState>> listJsonForRange(@RequestHeader(value="Range") String range, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        List<USState> body = null; 
        Range parsedRange = new Range(range.replaceAll(RANGE_PREFIX, ""));
        long count = USState.countUSStates();
        body = USState.findUSStateEntries(parsedRange.getFirstResult(), parsedRange.getMaxResults());
        headers.add(CONTENT_RANGE_HEADER, getContentRangeValue(parsedRange.getFirstResult(), body.size(), count));
        return new HttpEntity<List<USState>>(body, headers);
    }
    
    /**
     * TODO - This doesn't actually get selected since the query param is in the form of 'sort(...)' instead of 'sort=(...)'
     */
    @RequestMapping(method=RequestMethod.GET, headers={ACCEPT_JSON, "Range"}, params="sort")
    public @ResponseBody HttpEntity<List<USState>> listJsonForRangeSorted(@RequestHeader("Range") String range, @RequestParam("sort") String sort) {
        HttpHeaders headers = new HttpHeaders();
        List<USState> body = null; 
        Range parsedRange = new Range(range.replaceAll(RANGE_PREFIX, ""));
        long count = USState.countUSStates();
        //TODO - Implement sort param parsing
        body = USState.findOrderedUSStateEntries(parsedRange.getFirstResult(), parsedRange.getMaxResults(), "");
        headers.add(CONTENT_RANGE_HEADER, getContentRangeValue(parsedRange.getFirstResult(), body.size(), count));
        return new HttpEntity<List<USState>>(body, headers);
    }
    
    private String getContentRangeValue(Integer firstResult, Integer resultCount, Long totalCount) {
        StringBuilder value = new StringBuilder("items "+firstResult+"-");
        if (resultCount == 0) {
            value.append("0");
        } else {
            value.append(firstResult + resultCount - 1);
        }
        value.append("/"+totalCount);
        return value.toString();
    }
    
    private static final class Range {
        
        private Integer firstResult = 0;
        private Integer maxResults = 0;
        
        public Range(String range) {
            String[] parsed = range.split("-");
            Assert.isTrue(parsed.length == 2, "Range header in an unexpected format.");
            this.firstResult = new Integer(parsed[0]);
            Integer end = new Integer(parsed[1]);
            this.maxResults = end - firstResult + 1; 
        }
        
        public Integer getFirstResult() {
            return this.firstResult;
        }
        
        public Integer getMaxResults() {
            return this.maxResults;
        }
    }
    
}
