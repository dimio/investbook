/*
 * InvestBook
 * Copyright (C) 2021  Vitalii Ananev <spacious-team@ya.ru>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.investbook.web.forms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.investbook.repository.PortfolioRepository;
import ru.investbook.web.ControllerHelper;
import ru.investbook.web.forms.model.PortfolioCashModel;
import ru.investbook.web.forms.service.PortfolioCashFormsService;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.Collection;
import java.util.function.Supplier;

@Controller
@RequestMapping("/portfolio-cash")
@RequiredArgsConstructor
public class PortfolioCashController {
    private final PortfolioCashFormsService portfolioCashFormsService;
    private final PortfolioRepository portfolioRepository;
    private volatile Collection<String> portfolios;
    private volatile String selectedPortfolio;

    @PostConstruct
    public void start() {
        portfolios = ControllerHelper.getPortfolios(portfolioRepository);
    }

    @GetMapping
    public String get(Model model) {
        model.addAttribute("cashObjects", portfolioCashFormsService.getAll());
        return "portfolio-cash/table";
    }

    @GetMapping("/edit-form")
    public String getCashEditForm(@RequestParam(name = "id", required = false) Integer id, Model model) {
        PortfolioCashModel cash = getPortfolioCash(id, PortfolioCashModel::new);
        model.addAttribute("cash", cash);
        model.addAttribute("portfolios", portfolios);
        return "portfolio-cash/edit-form";

    }

    private PortfolioCashModel getPortfolioCash(Integer id, Supplier<? extends PortfolioCashModel> newSupplier) {
        if (id != null) {
            return portfolioCashFormsService.getById(id)
                    .orElseGet(newSupplier);
        } else {
            PortfolioCashModel model = newSupplier.get();
            model.setPortfolio(selectedPortfolio);
            return model;
        }
    }

    @PostMapping
    public String postCash(@Valid @ModelAttribute("cash") PortfolioCashModel cash) {
        selectedPortfolio = cash.getPortfolio();
        portfolioCashFormsService.save(cash);
        return "portfolio-cash/view-single";
    }


    @GetMapping("/delete")
    public String delete(@RequestParam(name = "id") Integer id, Model model) {
        portfolioCashFormsService.delete(id);
        model.addAttribute("message", "Запись удалена");
        model.addAttribute("backLink", "/portfolio-cash");
        return "success";
    }
}
