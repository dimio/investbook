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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.investbook.repository.SecurityRepository;
import ru.investbook.service.SecuritySectorService;
import ru.investbook.web.ControllerHelper;
import ru.investbook.web.forms.model.SecurityDescriptionModel;
import ru.investbook.web.forms.service.SecurityDescriptionFormsService;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@Controller
@RequestMapping("/security-descriptions")
@RequiredArgsConstructor
@Slf4j
public class SecurityDescriptionController {
    private final SecurityDescriptionFormsService securityDescriptionFormsService;
    private final SecuritySectorService securitySectorService;
    private final SecurityRepository securityRepository;
    private volatile Collection<String> securities;
    private volatile long securitiesCount = 0;

    @PostConstruct
    public void start() {
        securities = ControllerHelper.getSecuritiesDescriptions(securityRepository);
    }

    @GetMapping
    public String get(Model model) {
        setDefaultSecuritySectors();
        model.addAttribute("securityDescriptions", securityDescriptionFormsService.getAll());
        return "security-descriptions/table";
    }

    private void setDefaultSecuritySectors() {
        long count = securityRepository.count();
        if (count != securitiesCount) {
            securitySectorService.setDefaultSecuritySectors();
            securitiesCount = count;
        }
    }

    @GetMapping("update")
    public String updateFromSmartLab(@RequestParam(name = "security-id", required = false) Integer securityId,
                                     @RequestParam(name = "force", defaultValue = "false") boolean forceUpdate,
                                     Model model) {
        if (securityId == null) {
            String message = updateSectorsFromSmartLab(forceUpdate);
            model.addAttribute("message", message);
            model.addAttribute("backLink", "/security-descriptions");
            return "success";
        } else {
            securitySectorService.uploadAndUpdateSecuritySector(securityId, forceUpdate);
            return get(model);
        }
    }

    public String updateSectorsFromSmartLab(boolean forceUpdate) {
        securitySectorService.uploadAndUpdateSecuritySectors(forceUpdate);
        return "Список секторов выгружен со Smart-Lab страницы https://smart-lab.ru/forum/sectors";
    }

    @GetMapping("/edit-form")
    public String getEditForm(@RequestParam(name = "security-id", required = false) Integer securityId,
                              Model model) {
        model.addAttribute("securityDescription", getSecurityDescription(securityId));
        model.addAttribute("securities", securities);
        return "security-descriptions/edit-form";
    }

    private SecurityDescriptionModel getSecurityDescription(Integer securityId) {
        return Optional.ofNullable(securityId)
                .flatMap(securityDescriptionFormsService::getById)
                .orElseGet(SecurityDescriptionModel::new);
    }

    @PostMapping
    public String postTransaction(@Valid @ModelAttribute("securityDescription") SecurityDescriptionModel securityDescription) {
        securityDescriptionFormsService.save(securityDescription);
        return "security-descriptions/view-single";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam(name = "security-id") Integer securityId, Model model) {
        try {
            securityDescriptionFormsService.delete(securityId);
            model.addAttribute("message", "Инструмент удален");
        } catch (Exception e) {
            model.addAttribute("title", "Сработала защита");
            model.addAttribute("message", "Возможно по инструменту есть сделки или выплаты. " +
                    "Удалите их перед удалением инструмента.");
        }
        model.addAttribute("backLink", "/security-descriptions");
        return "success";
    }
}
