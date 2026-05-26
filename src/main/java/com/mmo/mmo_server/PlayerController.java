package com.mmo.mmo_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JWTServices jwtServices;

    @GetMapping("/players")
    public List<Players> getPlayers(){
        return playerRepository.findAll();
    }

    @PostMapping("/players")
    public ResponseEntity<?> createPlayer(@RequestBody Players player){
        if (playerRepository.findByUsername(player.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body("Username already taken!");
        }
        player.setPassword(passwordEncoder.encode(player.getPassword()));
        return ResponseEntity.ok(playerRepository.save(player));
    }

    @GetMapping("/players/{id}")
    public ResponseEntity<Players> getPlayer(@PathVariable Long id){
        return playerRepository.findById(id)
                .map(ResponseEntity :: ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/players/{id}")
    public String deletePlayer(@PathVariable Long id){
        playerRepository.deleteById(id);
        return "Player Deleted!";
    }

    @PutMapping("/players/{id}")
    public ResponseEntity<Players> updatePlayer(@RequestBody Players updatedData, @PathVariable Long id){
        return playerRepository.findById(id)
            .map(existingPlayer -> {
                existingPlayer.setUsername(updatedData.getUsername());
                existingPlayer.setHp(updatedData.getHp());
                existingPlayer.setGold(updatedData.getGold());
                existingPlayer.setXp(updatedData.getXp());
                existingPlayer.setLevel(updatedData.getLevel());

                return ResponseEntity.ok(playerRepository.save(existingPlayer));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Players loginData){
        return playerRepository.findByUsername(loginData.getUsername())
            .map(players -> {
                if (passwordEncoder.matches(loginData.getPassword(),players.getPassword())){
                    String token = jwtServices.generateToken(loginData.getUsername());
                    return ResponseEntity.ok(
                            Map.of(
                                 "token" , token,
                                    "username" ,players.getUsername(),
                                    "id" , players.getId()
                            ));
                }
                return ResponseEntity.status(401).body("Wrong password");

            }).orElse(ResponseEntity.status(404).body("Player Not Found!!"));
    }

    @GetMapping("/secure-test")
    public ResponseEntity<String> secureTest() {
        return ResponseEntity.ok("You are authenticated!");
    }

}
