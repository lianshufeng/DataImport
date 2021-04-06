package com.github.data.core.controller;

import com.github.data.core.helper.PathHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    @Autowired
    private PathHelper pathHelper;


    @RequestMapping({"/", ""})
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index.html");
        modelAndView.addObject("importFile", pathHelper.getInputPath().getAbsolutePath());
        modelAndView.addObject("exportFile", pathHelper.getExportPath().getAbsolutePath());
        return modelAndView;
    }


}
