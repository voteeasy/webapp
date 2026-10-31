package de.voteeasy.selfservice;

import org.junit.jupiter.api.Test;
import uk.org.okapibarcode.graphics.Color;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ColorMapperTest {

    @Test
    void getInkColorDefault() {
        assertThat(new QrCodeService.QrCodeParameter(null, null, null, Optional.empty(), Optional.empty()).getInkColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void getPaperColorDefault() {
        assertThat(new QrCodeService.QrCodeParameter(null, null, null, Optional.empty(), Optional.empty()).getPaperColor()).isEqualTo(Color.BLACK);
    }

    @Test
    void invalidColorCodeReturnsFallback() {
        assertThat(new QrCodeService.QrCodeParameter(null, null, null, Optional.of("poqwet"), Optional.empty()).getInkColor()).isEqualTo(Color.WHITE);
        assertThat(new QrCodeService.QrCodeParameter(null, null, null, Optional.empty(), Optional.of("pipi")).getPaperColor()).isEqualTo(Color.BLACK);
    }

    @Test
    void stringColorMatches() {
        assertThat(QrCodeService.ColorMapper.getColor(Optional.of("black"), Color.WHITE)).isEqualTo(Color.BLACK);
        assertThat(QrCodeService.ColorMapper.getColor(Optional.of("white"), Color.BLACK)).isEqualTo(Color.WHITE);
        assertThat(QrCodeService.ColorMapper.getColor(Optional.of("red"), Color.WHITE)).isEqualTo(Color.RED);
        assertThat(QrCodeService.ColorMapper.getColor(Optional.of("green"), Color.WHITE)).isEqualTo(Color.GREEN);
        assertThat(QrCodeService.ColorMapper.getColor(Optional.of("blue"), Color.WHITE)).isEqualTo(Color.BLUE);
    }

    @Test
    void rgbColorMatches() {
        assertThat(QrCodeService.ColorMapper.getColor(Optional.of("99AABB"), Color.WHITE)).isEqualTo(new Color(153, 170, 187));
        assertThat(QrCodeService.ColorMapper.getColor(Optional.of("invalidColor"), Color.WHITE)).isEqualTo(Color.WHITE);
    }

}
