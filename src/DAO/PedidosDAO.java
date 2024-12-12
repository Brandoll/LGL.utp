package DAO;

import Configuración.Conexion;
import Modelo.DetallePedido;
import Modelo.Pedidos;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

public class PedidosDAO {

    Connection con;
    Conexion cn = new Conexion();
    PreparedStatement ps;
    ResultSet rs;
    int r;

    public int IdPedido() {
        int id = 0;
        String sql = "SELECT MAX(id) FROM pedidos";
        try {
            con = cn.getConexion();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id;
    }

    public int verificarStado(int mesa, int id_sala) {
        int id_pedido = 0;
        String sql = "SELECT id FROM pedidos WHERE num_mesa=? AND id_sala=? AND estado = ?";
        try {
            con = cn.getConexion();
            ps = con.prepareStatement(sql);
            ps.setInt(1, mesa);
            ps.setInt(2, id_sala);
            ps.setString(3, "PENDIENTE");
            rs = ps.executeQuery();
            if (rs.next()) {
                id_pedido = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return id_pedido;
    }

    public boolean registrarPedidoConDetalles(Pedidos pedido, List<DetallePedido> detalles) {
        String sqlPedido = "INSERT INTO pedidos (id_sala, num_mesa, total, usuario) VALUES (?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedidos (nombre, precio, cantidad, comentario, id_pedido) VALUES (?, ?, ?, ?, ?)";

        try {
            con = cn.getConexion();
            con.setAutoCommit(false); // Inicia una transacción

            // Inserta el pedido principal
            ps = con.prepareStatement(sqlPedido, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, pedido.getId_sala());
            ps.setInt(2, pedido.getNum_mesa());
            ps.setDouble(3, pedido.getTotal());
            ps.setString(4, pedido.getUsuario());
            ps.executeUpdate();

            rs = ps.getGeneratedKeys(); // Obtiene el ID generado
            int idPedidoGenerado = 0;
            if (rs.next()) {
                idPedidoGenerado = rs.getInt(1);
            }

            // Inserta los detalles del pedido
            ps = con.prepareStatement(sqlDetalle);
            for (DetallePedido det : detalles) {
                ps.setString(1, det.getNombre());
                ps.setDouble(2, det.getPrecio());
                ps.setInt(3, det.getCantidad());
                ps.setString(4, det.getComentario());
                ps.setInt(5, idPedidoGenerado);
                ps.addBatch(); // Añade al lote
            }
            ps.executeBatch(); // Ejecuta todos los detalles en un solo paso

            con.commit(); // Confirma la transacción
            return true;
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback(); // Deshace los cambios en caso de error
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }

    public List verPedidoDetalle(int id_pedido) {
        List<DetallePedido> Lista = new ArrayList();
        String sql = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
        try {
            con = cn.getConexion();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id_pedido);
            rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido det = new DetallePedido();
                det.setId(rs.getInt("id"));
                det.setNombre(rs.getString("nombre"));
                det.setPrecio(rs.getDouble("precio"));
                det.setCantidad(rs.getInt("cantidad"));
                det.setComentario(rs.getString("comentario"));
                Lista.add(det);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return Lista;
    }

    public Pedidos verPedido(int id_pedido) {
        Pedidos ped = new Pedidos();
        String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
        try {
            con = cn.getConexion();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id_pedido);
            rs = ps.executeQuery();
            if (rs.next()) {

                ped.setId(rs.getInt("id"));
                ped.setFecha(rs.getString("fecha"));
                ped.setSala(rs.getString("nombre"));
                ped.setNum_mesa(rs.getInt("num_mesa"));
                ped.setTotal(rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return ped;
    }

    public List finalizarPedido(int id_pedido) {
        List<DetallePedido> Lista = new ArrayList();
        String sql = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
        try {
            con = cn.getConexion();
            ps = con.prepareStatement(sql);
            ps.setInt(1, id_pedido);
            rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido det = new DetallePedido();
                det.setId(rs.getInt("id"));
                det.setNombre(rs.getString("nombre"));
                det.setPrecio(rs.getDouble("precio"));
                det.setCantidad(rs.getInt("cantidad"));
                det.setComentario(rs.getString("comentario"));
                Lista.add(det);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return Lista;
    }

    public void pdfPedido(int id_pedido) {
        String fechaPedido = null, usuario = null, total = null, sala = null, num_mesa = null;
        try {
            FileOutputStream archivo;
            String url = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
            File salida = new File(url + File.separator + "Pedidos_LaGranjaLinda.pdf");
            archivo = new FileOutputStream(salida);
            Document doc = new Document(PageSize.A4, 30, 30, 20, 20); // Margen mejorado
            PdfWriter.getInstance(doc, archivo);
            doc.open();

            // Logotipo
            Image img = Image.getInstance(getClass().getResource("/Img/La Granja Linda.PNG"));
            img.scaleToFit(100, 100); // Ajustar tamaño del logotipo
            img.setAlignment(Element.ALIGN_LEFT);

            // Encabezado estilizado
            PdfPTable encabezado = new PdfPTable(2);
            encabezado.setWidthPercentage(100);
            encabezado.setSpacingBefore(10f);
            encabezado.setSpacingAfter(10f);
            float[] columnWidthsEncabezado = {20f, 80f};
            encabezado.setWidths(columnWidthsEncabezado);

            // Celda del logotipo
            PdfPCell logoCell = new PdfPCell(img);
            logoCell.setBorder(Rectangle.NO_BORDER);
            encabezado.addCell(logoCell);

            // Información de la empresa
            PdfPCell infoEmpresa = new PdfPCell();
            infoEmpresa.setBorder(Rectangle.NO_BORDER);
            infoEmpresa.setVerticalAlignment(Element.ALIGN_MIDDLE);
            String config = "SELECT * FROM config";
            try {
                con = cn.getConexion();
                ps = con.prepareStatement(config);
                rs = ps.executeQuery();
                if (rs.next()) {
                    infoEmpresa.addElement(new Paragraph(
                            rs.getString("nombre"),
                            new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY)
                    ));
                    infoEmpresa.addElement(new Paragraph("RUC: " + rs.getString("ruc")));
                    infoEmpresa.addElement(new Paragraph("Teléfono: " + rs.getString("telefono")));
                    infoEmpresa.addElement(new Paragraph("Dirección: " + rs.getString("direccion")));
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
            encabezado.addCell(infoEmpresa);
            doc.add(encabezado);

            // Información del pedido
            String informacion = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id WHERE p.id = ?";
            try {
                ps = con.prepareStatement(informacion);
                ps.setInt(1, id_pedido);
                rs = ps.executeQuery();
                if (rs.next()) {
                    num_mesa = rs.getString("num_mesa");
                    sala = rs.getString("nombre");
                    fechaPedido = rs.getString("fecha");
                    usuario = rs.getString("usuario");
                    total = rs.getString("total");
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }

            Paragraph infoPedido = new Paragraph("Detalles del Pedido\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK));
            infoPedido.add("Atendido por: " + usuario + "\n");
            infoPedido.add("N° Pedido: " + id_pedido + "\n");
            infoPedido.add("Fecha: " + fechaPedido + "\n");
            infoPedido.add("Sala: " + sala + "\n");
            infoPedido.add("N° Mesa: " + num_mesa + "\n");
            doc.add(infoPedido);

            doc.add(Chunk.NEWLINE);

            // Tabla de detalles
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            float[] columnWidths = {10f, 50f, 20f, 20f};
            tabla.setWidths(columnWidths);

            // Encabezados de la tabla
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            PdfPCell c1 = new PdfPCell(new Phrase("Cant.", headerFont));
            PdfPCell c2 = new PdfPCell(new Phrase("Plato", headerFont));
            PdfPCell c3 = new PdfPCell(new Phrase("P. Unit.", headerFont));
            PdfPCell c4 = new PdfPCell(new Phrase("P. Total", headerFont));
            BaseColor headerBackground = new BaseColor(100, 149, 237); // Azul claro

            c1.setBackgroundColor(headerBackground);
            c2.setBackgroundColor(headerBackground);
            c3.setBackgroundColor(headerBackground);
            c4.setBackgroundColor(headerBackground);

            tabla.addCell(c1);
            tabla.addCell(c2);
            tabla.addCell(c3);
            tabla.addCell(c4);

            // Detalles de los productos
            String product = "SELECT d.* FROM pedidos p INNER JOIN detalle_pedidos d ON p.id = d.id_pedido WHERE p.id = ?";
            try {
                ps = con.prepareStatement(product);
                ps.setInt(1, id_pedido);
                rs = ps.executeQuery();
                while (rs.next()) {
                    double subTotal = rs.getInt("cantidad") * rs.getDouble("precio");
                    tabla.addCell(rs.getString("cantidad"));
                    tabla.addCell(rs.getString("nombre"));
                    tabla.addCell(String.format("S/ %.2f", rs.getDouble("precio")));
                    tabla.addCell(String.format("S/ %.2f", subTotal));
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
            doc.add(tabla);

            // Total
            Paragraph totalParagraph = new Paragraph("\nTotal: S/ " + total,
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK));
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            doc.add(totalParagraph);

            // Mensaje de agradecimiento
            Paragraph gr = new Paragraph("\n¡Gracias por su compra!\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.DARK_GRAY));
            gr.setAlignment(Element.ALIGN_CENTER);
            doc.add(gr);

            doc.close();
            archivo.close();
            Desktop.getDesktop().open(salida);

        } catch (DocumentException | IOException e) {
            System.out.println(e.toString());
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }

    public boolean actualizarEstado(int id_pedido) {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";
        try {
            con = cn.getConexion();
            ps = con.prepareStatement(sql);
            ps.setString(1, "FINALIZADO");
            ps.setInt(2, id_pedido);
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        }
    }

    public List listarPedidos() {
        List<Pedidos> Lista = new ArrayList();
        String sql = "SELECT p.*, s.nombre FROM pedidos p INNER JOIN salas s ON p.id_sala = s.id ORDER BY p.fecha DESC";
        try {
            con = cn.getConexion();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Pedidos ped = new Pedidos();
                ped.setId(rs.getInt("id"));
                ped.setSala(rs.getString("nombre"));
                ped.setNum_mesa(rs.getInt("num_mesa"));
                ped.setFecha(rs.getString("fecha"));
                ped.setTotal(rs.getDouble("total"));
                ped.setUsuario(rs.getString("usuario"));
                ped.setEstado(rs.getString("estado"));
                Lista.add(ped);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return Lista;
    }

}
