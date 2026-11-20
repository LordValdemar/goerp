package br.com.goerp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class GoErpApplicationTests {

    @Test
    void contextoSobe() {
    }
}
