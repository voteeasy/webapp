package de.voteeasy.selfservice;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;

@Controller
@Slf4j
@RequestMapping("qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @Autowired
    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @RequestMapping(method = RequestMethod.GET, value = {
            // with default/fallback colors
            "/{tenant}/{user}/{password}",
            // with custom colors
            "/{tenant}/{user}/{password}/{ink}/{paper}"})
    public ResponseEntity<byte[]> generateQrCodeWithExternalParameters(HttpServletRequest request, @PathVariable String tenant, @PathVariable String user, @PathVariable String password, @PathVariable Optional<String> ink, @PathVariable Optional<String> paper) {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body( //
                this.qrCodeService.renderQrCode(request, //
                        new QrCodeService.QrCodeParameter(tenant, user, password, ink, paper)));
    }

}
