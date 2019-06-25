package pacote;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.ColorUIResource;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Frame extends javax.swing.JFrame {

    public static Frame frame;

    Arduino arduino = null;
    Connection conexão;
    CardLayout cl;
    CardLayout clBotoes;
    DefaultComboBoxModel modelo;
    boolean arduinoConectado = false;
    boolean edição = false;
    boolean existe;

    String id = "";
    String icone = null;

    public Frame() {
        controleDeInstancia();
        initComponents();
        listarPortas();
        conectar();
        ler();
    }

    public void listarPortas() {
        modelo.removeAllElements();
        modelo.addElement("SELECIONE UMA PORTA SERIAL");
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier porta = portEnum.nextElement();
            if (porta.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                modelo.addElement(porta.getName());
            }
        }
    }

    public void ligar() {
        statusArduino.setText("Arduino conectado com sucesso!");
        statusArduino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/sim.png")));
        arduinoConectado = true;
        main.setText("APROXIME O SEU CARTÃO DA LEITORA");
        label.setText("AGUARDANDO UM CARTÃO...");
        clBotoes.show(painelBotoes, "b2");
    }

    public void desligar(boolean semConexao) {
        try {
            arduinoConectado = false;
            if (!semConexao) {
                arduino.enviaDados(180);
                primeira = true;
            }
            arduino.close();
            statusArduino.setText("Arduino não conectado. Selecione uma porta");
            statusArduino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/nao.png")));
            boxPortas.setSelectedIndex(0);
            main.setText("SEM CONEXÃO COM O ARDUINO");
            label.setText("ARDUINO NÃO DETECTADO. ESCOLHA UMA PORTA");
            cl.show(mae, "p1");
            clBotoes.show(painelBotoes, "b1");
            boxPortas.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    
    private void initComponents() {

        mae = new javax.swing.JPanel();
        p1 = new javax.swing.JPanel();
        main = new javax.swing.JLabel();
        p2 = new javax.swing.JPanel();
        campoFoto = new javax.swing.JLabel();
        resultado = new javax.swing.JLabel();
        cartao = new javax.swing.JLabel();
        campoNome = new CampoNome();
        boxPermitir = new javax.swing.JComboBox<>();
        botaoSalvar = new javax.swing.JButton();
        voltar = new javax.swing.JLabel();
        painel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        rodape = new javax.swing.JPanel();
        statusArduino = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        b1 = new javax.swing.JPanel();
        boxPortas = new javax.swing.JComboBox<>();
        b2 = new javax.swing.JPanel();
        botaoDesligar = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Leitor de cartão");
        setResizable(false);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mae.setPreferredSize(new java.awt.Dimension(700, 330));
        mae.setLayout(new java.awt.CardLayout());
        cl = (CardLayout) mae.getLayout();

        main.setFont(new java.awt.Font("Arial Narrow", 1, 24)); 
        main.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        main.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/inicio.png"))); 
        main.setText("SEM CONEXÃO COM O ARDUINO");
        main.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        main.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        main.setIconTextGap(30);
        main.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        org.jdesktop.layout.GroupLayout p1Layout = new org.jdesktop.layout.GroupLayout(p1);
        p1.setLayout(p1Layout);
        p1Layout.setHorizontalGroup(
            p1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(p1Layout.createSequentialGroup()
                .addContainerGap()
                .add(main, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addContainerGap())
        );
        p1Layout.setVerticalGroup(
            p1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(p1Layout.createSequentialGroup()
                .addContainerGap()
                .add(main, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addContainerGap())
        );

        mae.add(p1, "p1");

        campoFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        campoFoto.setText("CLIQUE PARA ADICIONAR UMA FOTO");
        campoFoto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        campoFoto.setOpaque(true);
        campoFoto.setPreferredSize(new java.awt.Dimension(200, 250));
        campoFoto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoFotoMouseClicked(evt);
            }
        });

        resultado.setFont(new java.awt.Font("Arial Narrow", 0, 36)); 
        resultado.setForeground(new java.awt.Color(19, 173, 66));
        resultado.setPreferredSize(new java.awt.Dimension(453, 52));

        cartao.setFont(new java.awt.Font("Arial", 0, 24)); 
        cartao.setText("Cartão:");
        cartao.setPreferredSize(new java.awt.Dimension(453, 36));

        campoNome.setEditable(false);
        campoNome.setFont(new java.awt.Font("Arial", 0, 24)); 
        campoNome.setBorder(null);
        campoNome.setOpaque(false);
        campoNome.setPreferredSize(new java.awt.Dimension(453, 36));

        boxPermitir.setFont(new java.awt.Font("Arial", 0, 14)); 
        boxPermitir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Negar acesso", "Permitir acesso" }));
        boxPermitir.setFocusable(false);
        boxPermitir.setPreferredSize(new java.awt.Dimension(200, 36));
        boxPermitir.setVisible(false);
        botaoSalvar.setVisible(false);

        botaoSalvar.setFont(new java.awt.Font("Arial", 0, 14)); 
        botaoSalvar.setText("Salvar");
        botaoSalvar.setFocusable(false);
        botaoSalvar.setPreferredSize(new java.awt.Dimension(200, 34));
        botaoSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoSalvarActionPerformed(evt);
            }
        });

        voltar.setBackground(new java.awt.Color(230, 230, 230));
        voltar.setFont(new java.awt.Font("Arial", 0, 14)); 
        voltar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        voltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/voltar.png"))); 
        voltar.setText("Voltar");
        voltar.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        voltar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        voltar.setIconTextGap(8);
        voltar.setInheritsPopupMenu(false);
        voltar.setPreferredSize(new java.awt.Dimension(80, 80));
        voltar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        voltar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                voltarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                voltarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                voltarMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout p2Layout = new org.jdesktop.layout.GroupLayout(p2);
        p2.setLayout(p2Layout);
        p2Layout.setHorizontalGroup(
            p2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(p2Layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(campoFoto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(p2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resultado, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, p2Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(cartao, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(p2Layout.createSequentialGroup()
                        .add(p2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, boxPermitir, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, botaoSalvar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(voltar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(p2Layout.createSequentialGroup()
                        .add(campoNome, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        p2Layout.setVerticalGroup(
            p2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(p2Layout.createSequentialGroup()
                .add(38, 38, 38)
                .add(p2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(campoFoto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(p2Layout.createSequentialGroup()
                        .add(resultado, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cartao, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14)
                        .add(campoNome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(p2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, voltar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, p2Layout.createSequentialGroup()
                                .add(boxPermitir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(botaoSalvar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(42, Short.MAX_VALUE))
        );

        mae.add(p2, "p2");

        painel.setBackground(new java.awt.Color(221, 221, 221));
        painel.setPreferredSize(new java.awt.Dimension(700, 100));

        label.setFont(new java.awt.Font("Arial Black", 0, 23)); 
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("ARDUINO NÃO DETECTADO. ESCOLHA UMA PORTA");
        label.setPreferredSize(new java.awt.Dimension(680, 78));
        setIconImage(new ImageIcon(getClass().getResource("/pacote/icone.png")).getImage());

        org.jdesktop.layout.GroupLayout painelLayout = new org.jdesktop.layout.GroupLayout(painel);
        painel.setLayout(painelLayout);
        painelLayout.setHorizontalGroup(
            painelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(painelLayout.createSequentialGroup()
                .addContainerGap()
                .add(label, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelLayout.setVerticalGroup(
            painelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(painelLayout.createSequentialGroup()
                .addContainerGap()
                .add(label, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        rodape.setBackground(new java.awt.Color(221, 221, 221));
        rodape.setPreferredSize(new java.awt.Dimension(700, 70));

        statusArduino.setFont(new java.awt.Font("Arial", 0, 14)); 
        statusArduino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/nao.png"))); 
        statusArduino.setText("Arduino não conectado. Escolha uma porta");
        statusArduino.setIconTextGap(8);
        statusArduino.setName("0"); 
        statusArduino.setPreferredSize(new java.awt.Dimension(408, 48));

        painelBotoes.setOpaque(false);
        painelBotoes.setLayout(new java.awt.CardLayout());
        clBotoes = (CardLayout) painelBotoes.getLayout();

        b1.setOpaque(false);

        boxPortas.setFont(new java.awt.Font("Arial", 0, 15)); 
        boxPortas.setFocusable(false);
        boxPortas.setPreferredSize(new java.awt.Dimension(258, 38));
        boxPortas.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                boxPortasPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                boxPortasPopupMenuWillBecomeVisible(evt);
            }
        });
        modelo = new DefaultComboBoxModel();
        boxPortas.setModel(modelo);

        org.jdesktop.layout.GroupLayout b1Layout = new org.jdesktop.layout.GroupLayout(b1);
        b1.setLayout(b1Layout);
        b1Layout.setHorizontalGroup(
            b1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(b1Layout.createSequentialGroup()
                .addContainerGap()
                .add(boxPortas, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        b1Layout.setVerticalGroup(
            b1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, b1Layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .add(boxPortas, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        painelBotoes.add(b1, "b1");

        b2.setOpaque(false);

        botaoDesligar.setFont(new java.awt.Font("Arial", 0, 14)); 
        botaoDesligar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        botaoDesligar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/nao.png"))); 
        botaoDesligar.setText("Desligar conexão");
        botaoDesligar.setIconTextGap(8);
        botaoDesligar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                botaoDesligarMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botaoDesligarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botaoDesligarMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout b2Layout = new org.jdesktop.layout.GroupLayout(b2);
        b2.setLayout(b2Layout);
        b2Layout.setHorizontalGroup(
            b2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, b2Layout.createSequentialGroup()
                .addContainerGap(56, Short.MAX_VALUE)
                .add(botaoDesligar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 212, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        b2Layout.setVerticalGroup(
            b2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(b2Layout.createSequentialGroup()
                .addContainerGap()
                .add(botaoDesligar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelBotoes.add(b2, "b2");

        org.jdesktop.layout.GroupLayout rodapeLayout = new org.jdesktop.layout.GroupLayout(rodape);
        rodape.setLayout(rodapeLayout);
        rodapeLayout.setHorizontalGroup(
            rodapeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(rodapeLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusArduino, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(painelBotoes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        rodapeLayout.setVerticalGroup(
            rodapeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(rodapeLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusArduino, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(painelBotoes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(painel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(rodape, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(mae, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(painel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(mae, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(rodape, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }

    private void formMouseClicked(java.awt.event.MouseEvent evt) {
        mae.grabFocus();
    }

    boolean primeira = true;

    private void boxPortasPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
        String porta = (String) boxPortas.getSelectedItem();
        if (porta.equals("SELECIONE UMA PORTA SERIAL")) {
            return;
        }
        try {
            new Thread() {
                public void run() {
                    try {
                        if (arduino != null) {
                            arduino.close();
                        }
                        arduino = new Arduino(porta, 9600);
                        arduino.enviaDados(190);
                        boxPortas.setEnabled(false);
                        int i = 5;
                        boolean recebeu = false;
                        statusArduino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/talvez.png")));
                        while (i > -1) {
                            if(primeira) {
                                statusArduino.setText("Tentando conectar o arduino (" + i + " segundos) restantes");
                            } else {
                                statusArduino.setText("Estabelecendo a primeira conexão...");
                            }
                            int receberDados = arduino.receberDados();
                            if (receberDados == 49) {
                                recebeu = true;
                                break;
                            }
                            i--;
                            Thread.sleep(1000);
                        }
                        if (recebeu) {
                            ligar();
                        } else {
                            if (primeira) {
                                primeira = false;
                                boxPortasPopupMenuWillBecomeInvisible(null);
                            } else {
                                primeira = true;
                                JOptionPane.showMessageDialog(Frame.this, "O Arduino não foi detectado :(", "Erro", JOptionPane.ERROR_MESSAGE);
                                statusArduino.setText("Arduino não conectado. Selecione uma porta");
                                statusArduino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/nao.png")));
                                boxPortas.setSelectedIndex(0);
                                boxPortas.setEnabled(true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(Frame.this, "A porta não está disponível", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Frame.this, "Erro ao conectar", "Erro", JOptionPane.ERROR_MESSAGE);
            statusArduino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/nao.png")));
            boxPortas.setEnabled(true);
        }
    }

    private void boxPortasPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
        listarPortas();
    }

    private void botaoSalvarActionPerformed(java.awt.event.ActionEvent evt) {
        if (campoNome.getText().length() > 2 && campoNome.getText().length() < 50) {
            salvar();
        } else {
            JOptionPane.showMessageDialog(Frame.this, "Nome inválido", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void campoFotoMouseClicked(java.awt.event.MouseEvent evt) {
        if (!this.id.equals("") & edição) {
            if (evt.getButton() == 1 && evt.getClickCount() > 1) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Escolha uma foto");
                fc.setMultiSelectionEnabled(false);
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith("gif") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Imagens";
                    }
                });
                int i = fc.showOpenDialog(this);
                if (i == 0) {
                    try {
                        BufferedImage buffer = ImageIO.read(fc.getSelectedFile());
                        buffer = Thumbnails.of(buffer).size(200, 250).asBufferedImage();
                        ImageIcon icon = new ImageIcon(buffer);
                        campoFoto.setIcon(icon);
                        campoFoto.setText("");
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        ImageIO.write(buffer, "jpg", output);
                        byte[] bytes = output.toByteArray();
                        icone = Base64.encode(bytes);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(Frame.this, "Arquivo inválido", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            if (evt.getButton() == 3) {
                campoFoto.setIcon(null);
                campoFoto.setText("CLIQUE PARA ADICIONAR UMA FOTO");
                icone = null;
            }
        }
    }

    private void voltarMouseClicked(java.awt.event.MouseEvent evt) {
        voltar();
    }

    private void voltarMouseEntered(java.awt.event.MouseEvent evt) {
        voltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/voltar_sele.png")));
        voltar.setForeground(new java.awt.Color(30, 128, 200));
    }

    private void voltarMouseExited(java.awt.event.MouseEvent evt) {
        voltar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pacote/voltar.png")));
        voltar.setForeground(Color.black);
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        try {
            if (arduinoConectado) {
                arduino.enviaDados(180);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void botaoDesligarMouseClicked(java.awt.event.MouseEvent evt) {
        desligar(false);
    }

    private void botaoDesligarMouseEntered(java.awt.event.MouseEvent evt) {
        botaoDesligar.setForeground(new java.awt.Color(30, 128, 200));
    }

    private void botaoDesligarMouseExited(java.awt.event.MouseEvent evt) {
        botaoDesligar.setForeground(Color.black);
    }

    public void conectar() {
        try {
            DriverManager.setLoginTimeout(5);
            conexão = DriverManager.getConnection("jdbc:mysql:
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Frame.this, "Não foi possível conectar ao servidor", "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public void ler() {
        new Thread() {
            public void run() {
                String str = "";
                while (true) {
                    try {
                        if (arduinoConectado) {
                            int receberDados = arduino.receberDados();
                            if (receberDados != -1) {
                                char c = (char) receberDados;
                                if (c == '*' || c == '!') {
                                    if (c == '*') {
                                        edição(true);
                                    }
                                    if (c == '!') {
                                        edição(false);
                                    }
                                } else {
                                    if (c != '-') {
                                        str = str + c;
                                    } else {
                                        String id = str;
                                        str = "";
                                        ação(id);
                                    }
                                }
                            }
                        }
                        Thread.sleep(50);
                    } catch (Exception e) {
                        desligar(true);
                    }
                }

            }
        }.start();
    }

    public boolean verificarSeExiste(String id) {
        String sql = "SELECT * FROM cartoes WHERE id = " + id;
        try {
            PreparedStatement st = conexão.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return false;
        }
    }

    public void ação(String id) {
        existe = verificarSeExiste(id);
        this.id = id;
        cl.show(mae, "p2");
        limpar();
        if (!edição) {
            if (existe) {
                try {
                    String sql = "SELECT * FROM cartoes WHERE id = " + id;
                    PreparedStatement st = conexão.prepareStatement(sql);
                    ResultSet rs = st.executeQuery();
                    rs.next();
                    label.setText("LENDO CARTÃO NÚMERO: " + id);
                    cartao.setText("<html><span style=\"font-size:14px;\"><span>ID do Cartão: <strong>" + id + "</strong></span></span></html>");
                    String nome = rs.getString("nome");
                    int permitir = rs.getInt("permitir");
                    if (permitir == 0) {
                        resultado.setText("ACESSO NEGADO");
                        resultado.setForeground(new Color(230, 44, 44));
                        arduino.enviaDados(1);
                    }
                    if (permitir == 1) {
                        resultado.setText("ACESSO LIBERADO");
                        resultado.setForeground(new Color(53, 172, 53));
                        arduino.enviaDados(2);
                    }
                    icone = rs.getString("foto");
                    campoNome.setText(nome);
                    if (icone != null) {
                        byte[] base64byte = Base64.decode(icone);
                        ImageIcon im = new ImageIcon(base64byte);
                        if (im != null) {
                            campoFoto.setIcon(im);
                            campoFoto.setText("");
                        } else {
                            JOptionPane.showMessageDialog(Frame.this, "Imagem inválida", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(Frame.this, "Não foi possível baixar o conteúdo", "Erro", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            } else {
                label.setText("CARTÃO NÃO CADASTRADO");
                resultado.setText("CARTÃO NÃO CADASTRADO");
                cartao.setText("<html><span style=\"font-size:14px;\"><span>ID do Cartão: <strong>" + id + "</strong></span></span></html>");
                campoFoto.setText("");
                campoFoto.setIcon(new ImageIcon(getClass().getResource("/pacote/desconhecido.png")));
                resultado.setForeground(new Color(230, 44, 44));
                try {
                    arduino.enviaDados(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                if (existe) {
                    String sql = "SELECT * FROM cartoes WHERE id = " + id;
                    PreparedStatement st = conexão.prepareStatement(sql);
                    ResultSet rs = st.executeQuery();
                    rs.next();
                    label.setText("EDITANDO CADASTRO DE " + id);
                    cartao.setText("<html><span style=\"font-size:14px;\"><span>ID do Cartão: <strong>" + id + "</strong></span></span></html>");
                    resultado.setText("Editando cadastro");
                    String nome = rs.getString("nome");
                    int permitir = rs.getInt("permitir");
                    boxPermitir.setSelectedIndex(permitir);
                    icone = rs.getString("foto");
                    campoNome.setText(nome);
                    if (icone != null) {
                        byte[] base64byte = Base64.decode(icone);
                        ImageIcon im = new ImageIcon(base64byte);
                        if (im != null) {
                            campoFoto.setIcon(im);
                            campoFoto.setText("");
                        } else {
                            JOptionPane.showMessageDialog(Frame.this, "Imagem inválida", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    label.setText("CRIANDO CADASTRO DE " + id);
                    cartao.setText("<html><span style=\"font-size:14px;\"><span>ID do Cartão: <strong>" + id + "</strong></span></span></html>");
                    resultado.setText("Editando cadastro de " + id);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(Frame.this, "Não foi possível baixar o conteúdo", "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void salvar() {
        try {
            if (!existe) {
                String queryInserir = "INSERT INTO cartoes(id, nome, permitir, foto) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conexão.prepareStatement(queryInserir);
                ps.setString(1, id);
                ps.setString(2, campoNome.getText());
                ps.setString(3, boxPermitir.getSelectedIndex() + "");
                if (icone != null) {
                    ps.setBytes(4, icone.getBytes());
                } else {
                    ps.setBytes(4, null);
                }
                int resul = ps.executeUpdate();
                if (resul == 1) {
                    JOptionPane.showMessageDialog(this, "Informações salvas com sucesso!", "Sucesso", JOptionPane.PLAIN_MESSAGE, new javax.swing.ImageIcon(getClass().getResource("/pacote/sim.png")));
                } else {
                    JOptionPane.showMessageDialog(Frame.this, "Erro ao salvar informações. O banco de dados recusou o pedido", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String queryUpdate = "UPDATE cartoes SET nome = ?, permitir = ? , foto = ? WHERE id = " + id;
                PreparedStatement ps = conexão.prepareStatement(queryUpdate);
                ps.setString(1, campoNome.getText());
                ps.setString(2, boxPermitir.getSelectedIndex() + "");
                ps.setString(3, icone);
                int resul = ps.executeUpdate();
                if (resul == 1) {
                    JOptionPane.showMessageDialog(this, "Informações salvas com sucesso!", "Sucesso", JOptionPane.PLAIN_MESSAGE, new javax.swing.ImageIcon(getClass().getResource("/pacote/sim.png")));
                } else {
                    JOptionPane.showMessageDialog(Frame.this, "Erro ao salvar informações. O banco de dados recusou o pedido", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
            voltar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Frame.this, "Erro ao salvar informações", "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void voltar() {
        limpar();
        cl.show(mae, "p1");
        label.setText("AGUARDANDO UM CARTÃO...");
    }

    public void edição(boolean ativo) {
        if (ativo) {
            cl.show(mae, "p1");
            main.setText(
                    "<html>"
                    + "<p style=\"text-align: center;\"><span style=\"color:#ff0000;\"><span style=\"font-size:24px;\">* MODO EDIÇÃO *</span></span></p>\n"
                    + "<p style=\"text-align: center;\"><span style=\"font-size:14px;\">APROXIME O SEU CARTÃO DA LEITORA</span></p>"
                    + "</html>");
            edição = true;
            boxPermitir.setVisible(true);
            botaoSalvar.setVisible(true);
            campoNome.setEditable(true);
        } else {
            cl.show(mae, "p1");
            main.setText("APROXIME O SEU CARTÃO DA LEITORA");
            edição = false;
            boxPermitir.setVisible(false);
            botaoSalvar.setVisible(false);
            campoNome.setEditable(false);
            resultado.setForeground(Color.black);
        }
    }

    public void limpar() {
        campoFoto.setIcon(null);
        campoFoto.setText("CLIQUE PARA ADICIONAR UMA FOTO");
        cartao.setText("Cartão:");
        resultado.setText("");
        resultado.setForeground(Color.black);
        icone = null;
        campoNome.setText("");
        boxPermitir.setSelectedIndex(0);
    }

    public class CampoNome extends JTextField {

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().length() == 0 && edição) {
                Graphics2D d2 = (Graphics2D) g;
                d2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                d2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                d2.setFont(getFont());
                String texto = "Digite o nome do cadastro";
                FontMetrics metrics = d2.getFontMetrics(getFont());
                int y = (metrics.getAscent() + (getHeight() - (metrics.getAscent() + metrics.getDescent())) / 2);
                d2.drawString(texto, 0, y);
            }
        }
    }

    public class Arduino {

        private SerialPort serial;
        private OutputStream saida;
        private InputStream entrada;

        public Arduino(String porta, int taxa) throws Exception {
            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(porta);
            serial = (SerialPort) portId.open("Comunicação serial", taxa);
            saida = serial.getOutputStream();
            entrada = serial.getInputStream();
            
        }

        public void close() throws Exception {
            saida.close();
            entrada.close();
            serial.close();
        }
        
        public void enviaDados(int opcao) throws Exception {
            saida.write(opcao);
        }

        public void enviaDados(byte[] b) throws Exception {
            saida.write(b);
        }

        public int receberDados() throws Exception {
            return entrada.read();
        }

        public byte[] receberDadosByte() throws Exception {
            return IOUtils.toByteArray(entrada);
        }

    }

    private void controleDeInstancia() {
        File controleDeInstancia = new File(FileUtils.getTempDirectory() + "\\temp.temp");
        if (!controleDeInstancia.exists()) {
            try {
                controleDeInstancia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        try {
            if (controleDeInstancia.delete() == false) {
                File controleChamada = new File(FileUtils.getTempDirectory() + "\\chamada.temp");
                try {
                    controleChamada.createNewFile();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                System.exit(0);
            }
            FileChannel channel = new RandomAccessFile(controleDeInstancia, "rw").getChannel();
            FileLock lock = channel.lock();
        } catch (Exception e) {
            File controleChamada = new File(FileUtils.getTempDirectory() + "\\chamada.temp");
            try {
                controleChamada.createNewFile();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            System.exit(0);
        }
        new Thread() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        File controleChamada = new File(FileUtils.getTempDirectory() + "\\chamada.temp");
                        if (controleChamada.exists()) {
                            controleChamada.delete();
                            int sta = frame.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;
                            frame.setExtendedState(sta);
                            frame.setAlwaysOnTop(true);
                            frame.toFront();
                            frame.requestFocus();
                            frame.setAlwaysOnTop(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }.start();
    }

    public static void main(String args[]) throws Exception {
        javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.PLAIN, 12));
        UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
        frame = new Frame();
        frame.setVisible(true);
    }

    
    public javax.swing.JPanel b1;
    public javax.swing.JPanel b2;
    public javax.swing.JLabel botaoDesligar;
    public javax.swing.JButton botaoSalvar;
    public javax.swing.JComboBox<String> boxPermitir;
    public javax.swing.JComboBox<String> boxPortas;
    public javax.swing.JLabel campoFoto;
    public javax.swing.JTextField campoNome;
    public javax.swing.JLabel cartao;
    public javax.swing.JLabel label;
    public javax.swing.JPanel mae;
    public javax.swing.JLabel main;
    public javax.swing.JPanel p1;
    public javax.swing.JPanel p2;
    public javax.swing.JPanel painel;
    public javax.swing.JPanel painelBotoes;
    public javax.swing.JLabel resultado;
    public javax.swing.JPanel rodape;
    public javax.swing.JLabel statusArduino;
    public javax.swing.JLabel voltar;
    
}
