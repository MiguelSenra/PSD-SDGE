package org.example;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message1 implements Serializable {
    private Map<String, Integer> vv;
    private Map<String, Object> album;

    public Message1(Map<String, Integer> vv, Map<String, Object> album) {
        this.vv = vv;
        this.album = album;
    }

    public Map<String, Integer> getVv() {
        return vv;
    }

    public Map<String, Object> getAlbum() {
        return album;
    }

    public void setMessage(Map<String, Object> message) {
        this.album = message;
    }

    public void setVv(Map<String, Integer> vv) {
        this.vv = vv;
    }

    public int compareVectorClocks(Map<String, Integer> clock1,Map<String, Integer> clock2) {
        // Inicializa as variáveis para contar as chaves com valores maiores em cada relógio
        int largerInClock1 = 0;
        int largerInClock2 = 0;

        // Itera sobre as chaves do primeiro relógio
        for (Map.Entry<String, Integer> entry : clock1.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();

            // Verifica se a chave existe no segundo relógio
            if (clock2.containsKey(key)) {
                // Compara os valores das chaves
                if (value > clock2.get(key)) {
                    largerInClock1++;
                } else if (value < clock2.get(key)) {
                    largerInClock2++;
                }
            } else {
                // Se a chave não existir no segundo relógio, incrementa o contador para o primeiro relógio
                largerInClock1++;
            }
        }

        // Itera sobre as chaves do segundo relógio para verificar se há chaves exclusivas no segundo relógio
        for (String key : clock2.keySet()) {
            if (!clock1.containsKey(key)) {
                // Se a chave não existir no primeiro relógio, incrementa o contador para o segundo relógio
                largerInClock2++;
            }
        }

        // Compara os contadores para determinar a relação entre os relógios
        if (largerInClock1 > 0 && largerInClock2 == 0) {
            // Se todas as chaves com valores maiores estiverem no primeiro relógio e não houver chaves exclusivas no segundo relógio
            return 1; // O primeiro relógio é maior
        } else if (largerInClock1 == 0 && largerInClock2 > 0) {
            this.vv= clock2;
            // Se todas as chaves com valores maiores estiverem no segundo relógio e não houver chaves exclusivas no primeiro relógio
            return -1; // O segundo relógio é maior
        } else {
            // Caso contrário, não é possível determinar uma relação de ordem entre os relógios
            return 0; // Relógios são concorrentes
        }
    }

    public  Map<String, Integer> mergeClocks(Map<String, Integer> clock1, Map<String, Integer> clock2) {
        // Crie um novo mapa para o relógio mesclado
        Map<String, Integer> mergedClock = new HashMap<>();

        // Itera sobre as chaves do primeiro relógio
        for (Map.Entry<String, Integer> entry : clock1.entrySet()) {
            String key = entry.getKey();
            int value1 = entry.getValue();
            int value2 = clock2.getOrDefault(key, 0); // Obter o valor correspondente no segundo relógio, se existir

            // Mantenha o maior valor para cada chave
            mergedClock.put(key, Math.max(value1, value2));
        }

        // Itera sobre as chaves do segundo relógio para adicionar chaves exclusivas
        for (Map.Entry<String, Integer> entry : clock2.entrySet()) {
            String key = entry.getKey();
            int value2 = entry.getValue();
            if (!clock1.containsKey(key)) {
                // Se a chave não existir no primeiro relógio, adicione-a ao relógio mesclado
                mergedClock.put(key, value2);
            }
        }

        return mergedClock;
    }

    public Message1 mergeStates(AlbumCRDT state1) {
        // Criar um novo estado que será o resultado do merge
        Map<String, Object> mergedState = new HashMap<>();

        // Obter membros do álbum do estado 1
        Map<String, Map<String, Integer>> members1 = (Map<String, Map<String, Integer>>) state1.getAlbum().get("membros");
        Map<String, Map<String, Integer>> members2 = (Map<String, Map<String, Integer>>) this.album.get("membros");

        Map<String, Map<String, Integer>> newMembers = new HashMap<>();
        // Realizar merge para a chave "membros"
        if (members1 != null && members2 != null) {
            // Realizar merge para cada membro
            for (Map.Entry<String, Map<String, Integer>> entry : members1.entrySet()) {
                String member = entry.getKey();
                Map<String, Integer> clock1 = entry.getValue();

                if (members2.containsKey(member)) {
                    int comparisonResult = this.compareVectorClocks(clock1, members2.get(member));

                    if (comparisonResult > 0) {
                        // Se o relógio do membro no estado 1 for maior, adicione-o ao estado mesclado
                        newMembers.put(member, clock1);
                    } else if (comparisonResult < 0) {
                        // Se o relógio do membro no estado 2 for maior, adicione-o ao estado mesclado
                        newMembers.put(member, members2.get(member));
                    } else {
                        newMembers.put(member, mergeClocks(clock1, members2.get(member)));
                    }
                } else {
                    int cmpClocks= this.compareVectorClocks(clock1,this.vv);
                    if(cmpClocks==1){
                        newMembers.put(member, clock1);
                    }
                    else if (cmpClocks==0) {
                        Map<String, Integer> newClock = mergeClocks(clock1,state1.getVv());
                        newMembers.put(member, newClock);
                    }

                }
            }

            for (Map.Entry<String, Map<String, Integer>> entry : members2.entrySet()) {
                String member = entry.getKey();
                if (!members1.containsKey(member)) {
                    int cmpClocks= this.compareVectorClocks(entry.getValue(),state1.getVv());
                    if(cmpClocks==1){
                        newMembers.put(member, entry.getValue());
                    }
                    else if (cmpClocks==0) {
                        Map<String, Integer> newClock = mergeClocks(entry.getValue(),state1.getVv());
                        newMembers.put(member, newClock);
                    }
                }
            }


        }
        mergedState.put(
                "membros",
                newMembers
        );

        Map<String, Integer> newClock = mergeClocks(this.vv,state1.getVv());

        return new Message1(newClock,mergedState);

    }

}

