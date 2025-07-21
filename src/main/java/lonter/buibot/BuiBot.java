package lonter.buibot;

import lombok.val;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "lonter.buibot", "lonter.bat" })
public class BuiBot {
  void main(final String @NotNull[] args) {
    val app = new SpringApplication(BuiBot.class);

    app.setWebApplicationType(WebApplicationType.NONE);
    app.run(args);
  }
}