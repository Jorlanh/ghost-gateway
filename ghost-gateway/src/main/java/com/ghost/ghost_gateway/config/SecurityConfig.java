package com.ghost.ghost_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Libera o CORS (Permite a conversa com o React/Electron)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Desativa CSRF (pois usamos tokens JWT em vez de sessão web tradicional)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3. Regras de Acesso e Blindagem
            .authorizeHttpRequests(auth -> auth
                // BYPASS TEMPORÁRIO TÁTICO: Permite você testar o comando de voz no Frontend agora.
                // Quando o painel de login do Frontend estiver pronto, basta APAGAR esta linha:
                .requestMatchers("/api/v1/ghost/interact").permitAll() 
                
                // Rotas de monitoramento (ver se o serviço está vivo)
                .requestMatchers("/actuator/**").permitAll() 
                
                // BLOQUEIO TOTAL: Qualquer outra tentativa exige um Token Firebase JWT válido
                .anyRequest().authenticated()
            )
            
            // 4. Configura o Gateway para agir como um Validador de Tokens (OAuth2)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // O Acordo de Paz: Ensina o Gateway a aceitar pedidos vindos da porta do React
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Portas do Vite (8083 ou 5173) e do Electron
        configuration.setAllowedOrigins(List.of("http://localhost:8083", "http://localhost:5173", "electron://localhost"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todo o sistema
        return source;
    }
}