// Módulo: docs/artwork/gerador/ConceptArt (ferramenta, não faz parte do jogo)
// Propósito: desenha em Java2D o conceito artístico v0 (docs/artwork/conceito_artistico_v0.png):
//            o tabuleiro de 50 casas como uma torre de servidores com 5 pisos (zonas).
//            Correr: javac -encoding UTF-8 ConceptArt.java && java ConceptArt saida.png
// Aluno: [a preencher - responsável pelo mockup (C) depois de rever]
// Revisor: [a preencher]
// Uso de IA: Sim - gerado por Claude Code (ver AI_USE.md, secção 3). Excluído da contagem
//            de linhas de código do jogo (é uma ferramenta de arte, fora de src/).
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;

// Gera o conceito artístico v0 do ROOT ACCESS: o tabuleiro de 50 casas imaginado
// como uma torre de servidores com 5 pisos (zonas), coerente com o mapa do README.
public class ConceptArt {
  static final int W = 1600, H = 1000;
  static final int TW = 64, GAP = 6, THICK = 9, FLOOR_GAP = 148;
  static final double DX = 30, DY = -18;              // vetor de profundidade (oblíquo)
  static final int X0 = 770, Y0 = 892;                // canto do piso 0
  static final Color[] ZONE = { new Color(70,165,235), new Color(55,195,120), new Color(225,185,65), new Color(235,135,55), new Color(230,75,75) };
  static final String[] ZONE_NAME = {"INTERNET","DMZ","REDE INTERNA","DATACENTER","NÚCLEO"};
  static final Color ADV = new Color(0,230,120), FW = new Color(255,70,70), HP = new Color(255,165,30), ROOT = new Color(60,180,255);
  static final int[][] ADVANCES = {{4,13},{8,21},{16,27},{22,34},{28,39},{32,43},{36,46},{41,48}};
  static final int[] FIREWALLS = {11,25,35,44};
  static final int[][] HONEYPOTS = {{18,6},{30,19},{40,29}};
  static final String MONO = "Menlo", SANS = "Avenir Next";

  public static void main(String[] a) throws Exception {
    BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    background(g);
    tower(g);
    title(g);
    dice(g, 470, 560);
    terminal(g, 56, 704, 548, 238);
    g.setFont(new Font(MONO, Font.PLAIN, 13)); g.setColor(new Color(90,120,140));
    g.drawString("Conceito artístico v0 · Milestone 2 · novembro 2026", W - 430, H - 20);
    g.dispose();
    ImageIO.write(img, "png", new File(a[0]));
  }

  // ---------- fundo ----------
  static void background(Graphics2D g) {
    g.setPaint(new GradientPaint(0, 0, new Color(4,8,16), 0, H, new Color(10,22,36)));
    g.fillRect(0, 0, W, H);
    // brilho atrás do Root
    Point2D root = center(50);
    g.setPaint(new RadialGradientPaint(new Point2D.Double(root.getX(), root.getY() - 80), 520, new float[]{0f, 1f}, new Color[]{new Color(40,140,255,70), new Color(40,140,255,0)}));
    g.fillRect(0, 0, W, H);
    // grelha
    g.setStroke(new BasicStroke(1f));
    g.setColor(new Color(40,90,120,28));
    for (int x = 0; x < W; x += 40) g.drawLine(x, 0, x, H);
    for (int y = 0; y < H; y += 40) g.drawLine(0, y, W, y);
    // chuva binária
    Random r = new Random(50);
    g.setFont(new Font(MONO, Font.PLAIN, 12));
    for (int i = 0; i < 420; i++) {
      int x = r.nextInt(W), y = r.nextInt(H);
      g.setColor(new Color(0, 200, 160, 10 + r.nextInt(30)));
      g.drawString(r.nextBoolean() ? "1" : "0", x, y);
    }
  }

  // ---------- geometria ----------
  static int floorOf(int n) { return (n - 1) / 10; }
  static int colOf(int n) { int f = floorOf(n), i = (n - 1) % 10; return f % 2 == 0 ? i : 9 - i; }
  static Point2D corner(int n) { return new Point2D.Double(X0 + colOf(n) * TW, Y0 - floorOf(n) * FLOOR_GAP); }
  static Point2D center(int n) { Point2D c = corner(n); return new Point2D.Double(c.getX() + (TW - GAP) / 2.0 + DX / 2, c.getY() + DY / 2); }

  static Path2D topFace(double x, double y, double w) {
    Path2D p = new Path2D.Double(); p.moveTo(x, y); p.lineTo(x + w, y); p.lineTo(x + w + DX, y + DY); p.lineTo(x + DX, y + DY); p.closePath(); return p;
  }
  static Path2D frontFace(double x, double y, double w, double t) {
    Path2D p = new Path2D.Double(); p.moveTo(x, y); p.lineTo(x + w, y); p.lineTo(x + w, y + t); p.lineTo(x, y + t); p.closePath(); return p;
  }
  static Path2D sideFace(double x, double y, double t) {
    Path2D p = new Path2D.Double(); p.moveTo(x, y); p.lineTo(x + DX, y + DY); p.lineTo(x + DX, y + DY + t); p.lineTo(x, y + t); p.closePath(); return p;
  }
  static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a))); }

  static void glowStroke(Graphics2D g, Shape s, Color c, float w) {
    for (int k = 5; k >= 1; k--) { g.setStroke(new BasicStroke(w + k * 3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.setColor(alpha(c, 16)); g.draw(s); }
    g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.setColor(c); g.draw(s);
  }

  // ---------- torre ----------
  static void tower(Graphics2D g) {
    // postes do rack
    double left = X0 - 28, right = X0 + 10 * TW + 4;
    double top = Y0 - 4 * FLOOR_GAP - 40, bottom = Y0 + 30;
    for (double x : new double[]{left, right}) {
      for (double dx : new double[]{0, DX}) {
        Shape post = new Line2D.Double(x + dx, bottom + (dx > 0 ? DY : 0), x + dx, top + (dx > 0 ? DY : 0));
        glowStroke(g, post, new Color(40,110,150,160), 2.2f);
      }
    }
    // pisos de cima para baixo? desenha de baixo para cima para as ligações ficarem por cima
    for (int f = 0; f < 5; f++) floorSlab(g, f);
    // ligações da serpentina entre pisos (10->11, 20->21, ...)
    for (int n = 10; n < 50; n += 10) {
      Point2D a = center(n), b = center(n + 1);
      glowStroke(g, new Line2D.Double(a, b), new Color(40,170,210,150), 2f);
    }
    for (int n = 1; n <= 50; n++) tile(g, n);
    for (int fw : FIREWALLS) firewallWall(g, fw);
    for (int[] h : HONEYPOTS) beam(g, h[0], h[1], HP, true);
    for (int[] adv : ADVANCES) beam(g, adv[0], adv[1], ADV, false);
    rootServer(g);
    pawn(g, 23, new Color(0,255,140), "ANA", -10);
    pawn(g, 26, new Color(0,195,255), "RUI", 0);
    pawn(g, 9, new Color(255,210,50), "EVA", 0);
  }

  static void floorSlab(Graphics2D g, int f) {
    double x = X0 - 22, y = Y0 - f * FLOOR_GAP + 8, w = 10 * TW + 18;
    Color z = ZONE[f];
    g.setColor(new Color(10,20,30,235)); g.fill(topFace(x, y, w));
    g.setColor(new Color(6,12,20)); g.fill(frontFace(x, y, w, 14));
    g.setColor(new Color(8,15,24)); g.fill(sideFace(x + w, y, 14));
    g.setStroke(new BasicStroke(1.4f)); g.setColor(alpha(z, 200)); g.draw(topFace(x, y, w));
    glowStroke(g, new Line2D.Double(x, y + 14, x + w, y + 14), alpha(z, 150), 1.4f);
    // etiqueta da zona
    g.setFont(new Font(MONO, Font.BOLD, 15)); g.setColor(z);
    String label = ZONE_NAME[f];
    int lw = g.getFontMetrics().stringWidth(label);
    g.drawString(label, (int) x - 22 - lw, (int) y + 2);
    g.setFont(new Font(MONO, Font.PLAIN, 12)); g.setColor(new Color(120,150,170));
    String range = "casas " + (f * 10 + 1) + "–" + (f * 10 + 10);
    g.drawString(range, (int) x - 22 - g.getFontMetrics().stringWidth(range), (int) y + 18);
  }

  static int kind(int n) {
    for (int[] a : ADVANCES) if (a[0] == n) return 1;
    for (int f : FIREWALLS) if (f == n) return 2;
    for (int[] h : HONEYPOTS) if (h[0] == n) return 3;
    return n == 50 ? 4 : n == 1 ? 5 : 0;
  }

  static void tile(Graphics2D g, int n) {
    Point2D c = corner(n);
    double x = c.getX(), y = c.getY(), w = TW - GAP;
    int k = kind(n);
    Color z = ZONE[floorOf(n)];
    Color edge = switch (k) { case 1 -> ADV; case 2 -> FW; case 3 -> HP; case 4, 5 -> ROOT; default -> z; };
    Color fill = switch (k) { case 1 -> new Color(8,58,40); case 2 -> new Color(70,14,18); case 3 -> new Color(72,44,8); case 4, 5 -> new Color(10,40,80); default -> new Color(14,26,38); };
    g.setColor(fill.darker()); g.fill(frontFace(x, y, w, THICK));
    g.setColor(fill.darker().darker()); g.fill(sideFace(x + w, y, THICK));
    g.setPaint(new GradientPaint((float) x, (float) (y + DY), fill.brighter(), (float) x, (float) y, fill));
    g.fill(topFace(x, y, w));
    if (k != 0) glowStroke(g, topFace(x, y, w), alpha(edge, 230), 1.6f);
    else { g.setStroke(new BasicStroke(1.1f)); g.setColor(alpha(z, 170)); g.draw(topFace(x, y, w)); }
    g.setColor(alpha(edge, 120)); g.setStroke(new BasicStroke(1f)); g.draw(new Line2D.Double(x, y + THICK, x + w, y + THICK));
    // número
    g.setFont(new Font(MONO, Font.BOLD, 12));
    g.setColor(k == 0 ? new Color(190,215,230) : edge.brighter());
    String s = String.valueOf(n);
    g.drawString(s, (float) (x + 6), (float) (y + THICK + 15));
    // ícone no topo
    Point2D m = center(n);
    if (k == 1) { // chave estilizada + seta
      g.setColor(ADV); g.setStroke(new BasicStroke(2f));
      g.draw(new Ellipse2D.Double(m.getX() - 13, m.getY() - 4, 8, 8));
      g.draw(new Line2D.Double(m.getX() - 5, m.getY(), m.getX() + 6, m.getY()));
      g.draw(new Line2D.Double(m.getX() + 3, m.getY(), m.getX() + 3, m.getY() + 3));
      Path2D up = new Path2D.Double(); up.moveTo(m.getX() + 10, m.getY() + 3); up.lineTo(m.getX() + 14, m.getY() - 4); up.lineTo(m.getX() + 18, m.getY() + 3); up.closePath(); g.fill(up);
    } else if (k == 3) { // pote de mel (hexágono)
      Path2D hex = new Path2D.Double();
      for (int i = 0; i < 6; i++) { double ang = Math.PI / 3 * i; double px = m.getX() + 7 * Math.cos(ang), py = m.getY() + 4.5 * Math.sin(ang); if (i == 0) hex.moveTo(px, py); else hex.lineTo(px, py); }
      hex.closePath(); g.setColor(HP); g.fill(hex);
    }
  }

  static void firewallWall(Graphics2D g, int n) {
    Point2D c = corner(n);
    double x = c.getX() + 2, y = c.getY() + DY, w = TW - GAP - 4, h = 54;
    Path2D wall = new Path2D.Double(); wall.moveTo(x, y + 2); wall.lineTo(x + w, y + 2); wall.lineTo(x + w + DX * 0.2, y - h); wall.lineTo(x + DX * 0.2, y - h); wall.closePath();
    g.setPaint(new GradientPaint((float) x, (float) y, alpha(FW, 120), (float) x, (float) (y - h), alpha(FW, 10)));
    g.fill(wall);
    g.setColor(alpha(FW, 90)); g.setStroke(new BasicStroke(1f));
    for (double yy = y - 6; yy > y - h; yy -= 7) g.draw(new Line2D.Double(x + 2, yy, x + w - 2, yy));
    glowStroke(g, new Line2D.Double(x, y + 2, x + w, y + 2), FW, 1.6f);
    // escudo com "?"
    double sx = x + w / 2 - 9, sy = y - h / 2 - 14;
    Path2D shield = new Path2D.Double(); shield.moveTo(sx, sy); shield.lineTo(sx + 18, sy); shield.lineTo(sx + 18, sy + 12); shield.quadTo(sx + 18, sy + 22, sx + 9, sy + 26); shield.quadTo(sx, sy + 22, sx, sy + 12); shield.closePath();
    g.setColor(new Color(40,8,10)); g.fill(shield); glowStroke(g, shield, FW, 1.4f);
    g.setFont(new Font(MONO, Font.BOLD, 15)); g.setColor(Color.WHITE); g.drawString("?", (float) (sx + 5), (float) (sy + 17));
  }

  static void beam(Graphics2D g, int from, int to, Color color, boolean dashed) {
    Point2D a = center(from), b = center(to);
    double bulge = (colOf(to) - colOf(from)) * 6 + (dashed ? -60 : 60);
    CubicCurve2D curve = new CubicCurve2D.Double(a.getX(), a.getY(), a.getX() + bulge * 0.4, a.getY() + (b.getY() - a.getY()) * 0.15 - (dashed ? -20 : 40),
        b.getX() - bulge * 0.4, b.getY() - (b.getY() - a.getY()) * 0.15 + (dashed ? -40 : 30), b.getX(), b.getY());
    if (dashed) {
      for (int k = 4; k >= 1; k--) { g.setStroke(new BasicStroke(2f + k * 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.setColor(alpha(color, 14)); g.draw(curve); }
      g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{9f, 7f}, 0f)); g.setColor(color); g.draw(curve);
    } else glowStroke(g, curve, alpha(color, 230), 2.2f);
    // ponta de seta no destino
    double ang = Math.atan2(b.getY() - curve.getCtrlY2(), b.getX() - curve.getCtrlX2());
    Path2D head = new Path2D.Double(); head.moveTo(b.getX(), b.getY());
    head.lineTo(b.getX() - 12 * Math.cos(ang - 0.45), b.getY() - 12 * Math.sin(ang - 0.45));
    head.lineTo(b.getX() - 12 * Math.cos(ang + 0.45), b.getY() - 12 * Math.sin(ang + 0.45)); head.closePath();
    g.setColor(color); g.fill(head);
    g.setColor(alpha(color, 90)); g.fill(new Ellipse2D.Double(a.getX() - 6, a.getY() - 4, 12, 8));
  }

  static void rootServer(Graphics2D g) {
    Point2D m = center(50);
    double w = 46, h = 104, x = m.getX() - w / 2 - 4, y = m.getY() - h + 2;
    // feixe de luz
    g.setPaint(new GradientPaint((float) m.getX(), (float) y, alpha(ROOT, 90), (float) m.getX(), 40f, alpha(ROOT, 0)));
    Path2D beam = new Path2D.Double(); beam.moveTo(x + 4, y); beam.lineTo(x + w - 4 + DX * 0.5, y); beam.lineTo(x + w + 40, 40); beam.lineTo(x - 40, 40); beam.closePath(); g.fill(beam);
    // corpo do servidor (3 faces)
    g.setColor(new Color(10,30,60)); g.fill(new Rectangle2D.Double(x, y, w, h));
    Path2D side = new Path2D.Double(); side.moveTo(x + w, y); side.lineTo(x + w + DX * 0.5, y + DY * 0.5); side.lineTo(x + w + DX * 0.5, y + h + DY * 0.5); side.lineTo(x + w, y + h); side.closePath();
    g.setColor(new Color(6,18,40)); g.fill(side);
    Path2D top = new Path2D.Double(); top.moveTo(x, y); top.lineTo(x + w, y); top.lineTo(x + w + DX * 0.5, y + DY * 0.5); top.lineTo(x + DX * 0.5, y + DY * 0.5); top.closePath();
    g.setColor(new Color(30,80,140)); g.fill(top);
    glowStroke(g, new Rectangle2D.Double(x, y, w, h), ROOT, 1.6f);
    for (int i = 0; i < 6; i++) {
      double yy = y + 10 + i * 15;
      g.setColor(new Color(20,60,100)); g.fill(new RoundRectangle2D.Double(x + 6, yy, w - 12, 9, 3, 3));
      g.setColor(i % 2 == 0 ? new Color(0,255,160) : new Color(80,200,255)); g.fill(new Ellipse2D.Double(x + w - 14, yy + 2, 5, 5));
    }
    g.setFont(new Font(MONO, Font.BOLD, 20));
    String s = "ROOT";
    int sw = g.getFontMetrics().stringWidth(s);
    for (int k = 4; k >= 1; k--) { g.setColor(alpha(ROOT, 40)); g.drawString(s, (float) (x + w / 2 - sw / 2.0 - k * 0.5), (float) (y - 14)); }
    g.setColor(new Color(200,235,255)); g.drawString(s, (float) (x + w / 2 - sw / 2.0), (float) (y - 14));
  }

  static void pawn(Graphics2D g, int n, Color c, String name, int dx) {
    Point2D m = center(n);
    double x = m.getX() + dx, y = m.getY();
    g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), 26, new float[]{0f, 1f}, new Color[]{alpha(c, 120), alpha(c, 0)}));
    g.fill(new Ellipse2D.Double(x - 26, y - 16, 52, 32));
    g.setColor(new Color(0,0,0,120)); g.fill(new Ellipse2D.Double(x - 12, y - 4, 24, 9));
    // corpo cónico
    Path2D body = new Path2D.Double(); body.moveTo(x - 11, y); body.lineTo(x - 5, y - 26); body.lineTo(x + 5, y - 26); body.lineTo(x + 11, y); body.closePath();
    g.setPaint(new GradientPaint((float) (x - 11), 0f, c.darker(), (float) (x + 11), 0f, c.brighter()));
    g.fill(body);
    g.fill(new Ellipse2D.Double(x - 11, y - 4, 22, 8));
    // cabeça
    g.setPaint(new RadialGradientPaint(new Point2D.Double(x - 3, y - 36), 10, new float[]{0f, 1f}, new Color[]{Color.WHITE, c}));
    g.fill(new Ellipse2D.Double(x - 8, y - 42, 16, 16));
    g.setColor(alpha(Color.WHITE, 160)); g.setStroke(new BasicStroke(1f)); g.draw(new Ellipse2D.Double(x - 8, y - 42, 16, 16));
    // etiqueta
    g.setFont(new Font(MONO, Font.BOLD, 12));
    int w = g.getFontMetrics().stringWidth(name) + 12;
    RoundRectangle2D tag = new RoundRectangle2D.Double(x - w / 2.0, y - 66, w, 18, 8, 8);
    g.setColor(new Color(5,12,20,220)); g.fill(tag); g.setColor(c); g.draw(tag);
    g.drawString(name, (float) (x - w / 2.0 + 6), (float) (y - 53));
  }

  // ---------- título ----------
  static void title(Graphics2D g) {
    Font big = new Font(SANS, Font.BOLD, 84);
    g.setFont(big);
    String t = "ROOT ACCESS";
    for (int k = 8; k >= 1; k--) {
      g.setColor(new Color(0, 230, 180, 10));
      for (int dx = -k; dx <= k; dx += k) for (int dy = -k; dy <= k; dy += k) g.drawString(t, 56 + dx, 150 + dy);
    }
    g.setPaint(new GradientPaint(56, 90, new Color(120,255,220), 56, 160, new Color(0,200,160)));
    g.drawString(t, 56, 150);
    g.setFont(new Font(SANS, Font.PLAIN, 36)); g.setColor(new Color(200,225,240));
    g.drawString("Subida ao Servidor", 60, 202);
    g.setFont(new Font(MONO, Font.PLAIN, 16)); g.setColor(new Color(110,170,195));
    g.drawString("> 2–4 pentesters · 50 casas · 5 zonas · 1 Root", 62, 244);
    // legenda dos efeitos
    int y = 300;
    legend(g, 62, y, ADV, "8 brechas", "avançam para a frente (ex.: 4 → 13)");
    legend(g, 62, y + 40, FW, "4 firewalls", "pergunta: certa avança 1 casa");
    legend(g, 62, y + 80, HP, "3 honeypots", "recuam (ex.: 18 → 6)");
    legend(g, 62, y + 120, ROOT, "casa 50", "chegada exata ao Root (ressalto)");
  }
  static void legend(Graphics2D g, int x, int y, Color c, String bold, String rest) {
    g.setColor(alpha(c, 50)); g.fill(new RoundRectangle2D.Double(x, y - 16, 22, 22, 6, 6));
    glowStroke(g, new RoundRectangle2D.Double(x, y - 16, 22, 22, 6, 6), c, 1.4f);
    g.setFont(new Font(MONO, Font.BOLD, 16)); g.setColor(c); g.drawString(bold, x + 36, y);
    int w = g.getFontMetrics().stringWidth(bold);
    g.setFont(new Font(MONO, Font.PLAIN, 15)); g.setColor(new Color(170,195,210)); g.drawString(" — " + rest, x + 36 + w, y);
  }

  // ---------- dado ----------
  static void dice(Graphics2D g, double cx, double cy) {
    double s = 62;
    Point2D top = new Point2D.Double(cx, cy - s), right = new Point2D.Double(cx + s * 0.95, cy - s * 0.45), left = new Point2D.Double(cx - s * 0.95, cy - s * 0.45);
    Point2D mid = new Point2D.Double(cx, cy + s * 0.1), bl = new Point2D.Double(cx - s * 0.95, cy + s * 0.7), br = new Point2D.Double(cx + s * 0.95, cy + s * 0.7), bot = new Point2D.Double(cx, cy + s * 1.25);
    g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), 150, new float[]{0f, 1f}, new Color[]{new Color(0,230,180,60), new Color(0,230,180,0)}));
    g.fill(new Ellipse2D.Double(cx - 150, cy - 150, 300, 300));
    Path2D topF = poly(top, right, mid, left), leftF = poly(left, mid, bot, bl), rightF = poly(mid, right, br, bot);
    g.setPaint(new GradientPaint((float) cx, (float) (cy - s), new Color(40,80,100), (float) cx, (float) mid.getY(), new Color(22,47,61))); g.fill(topF);
    g.setColor(new Color(14,30,42)); g.fill(leftF);
    g.setColor(new Color(9,20,30)); g.fill(rightF);
    for (Path2D f : new Path2D[]{topF, leftF, rightF}) glowStroke(g, f, new Color(0,210,170), 1.6f);
    // 3 pips no topo: a face do último lançamento (Eva, 6 -> 9)
    double[][] uv = {{.25,.25},{.5,.5},{.75,.75}};
    for (double[] p : uv) pip(g, top, right, left, p[0], p[1], 9, new Color(230,250,245));
    // faces laterais coerentes com um dado real: 2 à esquerda e 6 à direita
    pip(g, left, mid, bl, .3, .3, 7, new Color(170,200,200)); pip(g, left, mid, bl, .7, .7, 7, new Color(170,200,200));
    for (double u : new double[]{.25, .5, .75}) { pip(g, mid, right, bot, u, .28, 6, new Color(150,180,185)); pip(g, mid, right, bot, u, .72, 6, new Color(150,180,185)); }
    g.setFont(new Font(MONO, Font.BOLD, 15)); g.setColor(new Color(0,230,140));
    g.drawString("SAIU 3", (float) (cx - 28), (float) (bot.getY() + 30));
  }
  static Path2D poly(Point2D... p) { Path2D path = new Path2D.Double(); path.moveTo(p[0].getX(), p[0].getY()); for (int i = 1; i < p.length; i++) path.lineTo(p[i].getX(), p[i].getY()); path.closePath(); return path; }
  // ponto (u,v) no paralelogramo definido por origem o, eixo a (o->a) e eixo b (o->b)
  static void pip(Graphics2D g, Point2D o, Point2D a, Point2D b, double u, double v, double r, Color c) {
    double x = o.getX() + (a.getX() - o.getX()) * u + (b.getX() - o.getX()) * v;
    double y = o.getY() + (a.getY() - o.getY()) * u + (b.getY() - o.getY()) * v;
    g.setColor(c); g.fill(new Ellipse2D.Double(x - r, y - r * 0.62, 2 * r, 2 * r * 0.62));
  }

  // ---------- terminal ----------
  static void terminal(Graphics2D g, int x, int y, int w, int h) {
    RoundRectangle2D box = new RoundRectangle2D.Double(x, y, w, h, 14, 14);
    g.setColor(new Color(5,10,16,235)); g.fill(box);
    glowStroke(g, box, new Color(25,95,110), 1.4f);
    g.setColor(new Color(12,24,34)); g.fill(new RoundRectangle2D.Double(x, y, w, 30, 14, 14)); g.fillRect(x, y + 16, w, 14);
    Color[] dots = {new Color(255,95,90), new Color(255,190,50), new Color(40,200,90)};
    for (int i = 0; i < 3; i++) { g.setColor(dots[i]); g.fillOval(x + 14 + i * 18, y + 10, 10, 10); }
    g.setFont(new Font(MONO, Font.BOLD, 13)); g.setColor(new Color(90,200,220)); g.drawString("log de eventos — ronda 8/40", x + 80, y + 20);
    String[][] lines = {
      {"> EVA lançou 4 (dado aleatório): casa 14 -> 18.", "255,175,60"},
      {"  Honeypot da DMZ! 18 -> 6.", "255,175,60"},
      {"> ANA lançou 3 (dado aleatório): casa 20 -> 23.", "0,230,140"},
      {"> RUI lançou 1 (dado aleatório): 24 -> 25. Firewall!", "255,110,110"},
      {"  A) Pass-the-hash: certa! Passa para a casa 26.", "0,230,140"},
      {"> EVA lançou 3 (dado aleatório): casa 6 -> 9.", "0,230,140"},
      {"> vez de ANA · posições (23, 26, 9)", "90,200,220"},
    };
    g.setFont(new Font(MONO, Font.PLAIN, 13));
    for (int i = 0; i < lines.length; i++) {
      String[] rgb = lines[i][1].split(",");
      g.setColor(new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
      g.drawString(lines[i][0], x + 18, y + 56 + i * 24);
    }
    g.setColor(new Color(0,230,140)); g.fillRect(x + 18, y + 56 + lines.length * 24 - 12, 9, 15);
  }
}
