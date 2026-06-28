import sys
path = "src/main/java/com/vnmine/cultivation/MeditationManager.java"
f=open(path)
c=f.read()
f.close()

old_start = "                    // Fire rings effect"
new_start = "                    // Fire rings effect - 2 vong lua doi xung qua nguoi choi"

if old_start in c:
    idx = c.find(old_start)
    # Find the end of this section (}
    end_idx = c.find("                    }", idx)
    end_idx = c.find("                    }", end_idx + 20)
    end_idx = c.find("                    }", end_idx + 20) 
    old_block = c[idx:end_idx+24]
    
    new_block = """                    // Fire rings effect - 2 vong lua doi xung qua nguoi choi
                    if (config.isFireRingEnabled()) {
                        Location origin = session.getOriginalLocation().clone().add(0, config.getFireRingYOffset(), 0);
                        float angle = session.getRotationAngle();
                        double r = config.getFireRingRadius();
                        int pCount = config.getFireRingCount();
                        Particle fParticle = config.getFireRingParticle();
                        double pSpeed = config.getFireRingSpeed();
                        // Ring 1
                        for (int i = 0; i < pCount; i++) {
                            double a = (Math.PI * 2 / pCount) * i;
                            double x = r * Math.cos(angle + a);
                            double z = r * Math.sin(angle + a);
                            origin.getWorld().spawnParticle(fParticle, origin.clone().add(x, 0, z), 1, 0, 0, 0, pSpeed);
                        }
                        // Ring 2 (doi xung - lech pha 180 do)
                        for (int i = 0; i < pCount; i++) {
                            double a = (Math.PI * 2 / pCount) * i + Math.PI;
                            double x = r * Math.cos(angle + a);
                            double z = r * Math.sin(angle + a);
                            origin.getWorld().spawnParticle(fParticle, origin.clone().add(x, 0, z), 1, 0, 0, 0, pSpeed);
                        }
                    }"""
    
    c = c.replace(old_block, new_block)
    f=open(path,"w")
    f.write(c)
    f.close()
    print("Fire ring code replaced successfully")
else:
    print("old_start not found!")
    sys.exit(1)
