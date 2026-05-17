package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class MainFrame extends JFrame {

    private JLabel timerLabel;
    private JButton pauseButton;
    private JButton deleteButton;

    private Timer timer;

    private int segundos = 0;

    public MainFrame() {

        setTitle("Tickly");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        criarComponentes();
        configurarTimer();

        setVisible(true);
    }

    private void criarComponentes() {

        setLayout(new BorderLayout());

        // RELÓGIO
        timerLabel = new JLabel("00:00:00");
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 50));

        add(timerLabel, BorderLayout.CENTER);

        // TEMPO TOTAL
        totalLabel = new JLabel("Tempo Total: 00:00:00");

        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        totalLabel.setFont(new Font("Arial", Font.PLAIN, 20));

        add(totalLabel, BorderLayout.NORTH);

        // BOTÕES
        JPanel buttonPanel = new JPanel();

        pauseButton = new JButton("Play");
        deleteButton = new JButton("🗑");

        buttonPanel.add(pauseButton);
        buttonPanel.add(deleteButton);

        // LISTA DE TEMPOS SALVOS
        listaModel = new DefaultListModel<>();

        listaTempos = new JList<>(listaModel);

        JScrollPane scrollPane = new JScrollPane(listaTempos);

        scrollPane.setPreferredSize(new Dimension(180, 0));

        add(scrollPane, BorderLayout.EAST);

        add(buttonPanel, BorderLayout.SOUTH);

        // EVENTOS

        pauseButton.addActionListener(e -> {

            // PLAY
            if (pauseButton.getText().equals("Play")) {

                timer.start();

                pauseButton.setText("Pause");

                emIntervalo = false;
            }

            // PAUSE
            else {

                abrirPopupPause(null);
            }
        });

        deleteButton.addActionListener(e -> excluirMedicao());
    }

    private void configurarTimer() {
        carregarMedicoes();
        atualizarTempoTotal();

        timer = new Timer(1000, e -> {

            segundos++;

            int horas = segundos / 3600;
            int minutos = (segundos % 3600) / 60;
            int secs = segundos % 60;

            String tempo = String.format(
                    "%02d:%02d:%02d",
                    horas,
                    minutos,
                    secs);

            timerLabel.setText(tempo);

        });
    }

    private void abrirPopupPause(ActionEvent e) {

        timer.stop();

        Object[] opcoes = { "Salvar", "Continuar", "Intervalo" };

        int escolha = JOptionPane.showOptionDialog(
                this,
                "Cronômetro pausado",
                "Pause",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opcoes,
                opcoes[1]);

        // SALVAR
        if (escolha == 0) {

            String titulo = JOptionPane.showInputDialog(
                    this,
                    "Digite um título:");

            if (titulo != null && !titulo.isEmpty()) {

                String tempoAtual = timerLabel.getText();

                listaModel.addElement(
                        titulo + " - " + tempoAtual);

                atualizarTempoTotal();

                // RESETAR CRONÔMETRO
                segundos = 0;
                timerLabel.setText("00:00:00");

                pauseButton.setText("Play");
                emIntervalo = false;
            }

        }

        // CONTINUAR
        else if (escolha == 1) {

            timer.start();
        }

        // INTERVALO
        else if (escolha == 2) {

            emIntervalo = true;

            pauseButton.setText("Play");
        }
    }

    private void excluirMedicao() {

        int indiceSelecionado = listaTempos.getSelectedIndex();

        if (indiceSelecionado != -1) {

            listaModel.remove(indiceSelecionado);

            atualizarTempoTotal();
            salvarMedicoes();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Selecione uma medição para excluir.");
        }
    }

    private void atualizarTempoTotal() {
        salvarMedicoes();

        int totalSegundos = 0;

        for (int i = 0; i < listaModel.size(); i++) {

            String item = listaModel.get(i);

            String tempo = item.split(" - ")[1];

            String[] partes = tempo.split(":");

            int horas = Integer.parseInt(partes[0]);
            int minutos = Integer.parseInt(partes[1]);
            int segundos = Integer.parseInt(partes[2]);

            totalSegundos += horas * 3600;
            totalSegundos += minutos * 60;
            totalSegundos += segundos;
        }

        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segundos = totalSegundos % 60;

        String totalFormatado = String.format(
                "%02d:%02d:%02d",
                horas,
                minutos,
                segundos);

        totalLabel.setText(
                "Tempo Total: " + totalFormatado);
    }

    private void salvarMedicoes() {

        try {

            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(ARQUIVO));

            for (int i = 0; i < listaModel.size(); i++) {

                writer.write(listaModel.get(i));
                writer.newLine();
            }

            writer.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void carregarMedicoes() {

        File arquivo = new File(ARQUIVO);

        if (!arquivo.exists()) {
            return;
        }

        try {

            BufferedReader reader = new BufferedReader(
                    new FileReader(arquivo));

            String linha;

            while ((linha = reader.readLine()) != null) {

                listaModel.addElement(linha);
            }

            reader.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private DefaultListModel<String> listaModel;
    private JList<String> listaTempos;
    private boolean emIntervalo = false;
    private JLabel totalLabel;
    private final String ARQUIVO = "medicoes.txt";
}