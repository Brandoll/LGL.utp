package DAO;

import Configuración.Conexion;
import Modelo.Platos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlatosDAO {

    Conexion cn = new Conexion(); // Instancia de la clase Conexion
    PreparedStatement ps;
    ResultSet rs;

    // Registrar un plato en la base de datos
    public boolean Registrar(Platos pla) {
        String sql = "INSERT INTO platos (nombre, precio, fecha) VALUES (?,?,?)";
        Connection con = null;
        try {
            con = cn.getConexion(); // Obtener una nueva conexión
            ps = con.prepareStatement(sql);
            ps.setString(1, pla.getNombre());
            ps.setDouble(2, pla.getPrecio());
            ps.setDate(3, pla.getFecha());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close(); // Cerrar conexión después de usarla
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    public List<Platos> Listar(String valor) {
        List<Platos> Lista = new ArrayList<>();
        String sql = "SELECT * FROM platos";
        String consultaPorValor = "SELECT * FROM platos WHERE nombre LIKE ?";
        Connection con = null;

        try {
            con = cn.getConexion();

            if (!valor.isEmpty()) {
                ps = con.prepareStatement(consultaPorValor);
                ps.setString(1, "%" + valor + "%");
            } else {
                ps = con.prepareStatement(sql);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Platos pl = new Platos();
                pl.setId(rs.getInt("id"));
                pl.setNombre(rs.getString("nombre"));
                pl.setPrecio(rs.getDouble("precio"));
                pl.setFecha(rs.getDate("fecha"));
                Lista.add(pl);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
        return Lista;
    }

    // Eliminar un plato por ID
    public boolean Eliminar(int id) {
        String sql = "DELETE FROM platos WHERE id = ?";
        Connection con = null;
        try {
            con = cn.getConexion(); // Obtener una nueva conexión
            ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.execute();

            // Reasignar IDs después de eliminar
            ReasignarIDs();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close(); // Cerrar conexión después de usarla
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    // Método para reasignar los IDs de la tabla 'platos'
    private void ReasignarIDs() {
        Connection con = null;
        try {
            con = cn.getConexion();

            // Iniciar una transacción para evitar problemas con otras conexiones
            con.setAutoCommit(false);

            // Reasignar IDs secuencialmente
            ps = con.prepareStatement("SET @count = 0");
            ps.execute();
            ps = con.prepareStatement("UPDATE platos SET id = (@count := @count + 1) ORDER BY id");
            ps.execute();

            // Ajustar el valor de AUTO_INCREMENT al máximo ID actual
            ps = con.prepareStatement("SELECT MAX(id) FROM platos");
            rs = ps.executeQuery();
            if (rs.next()) {
                int maxId = rs.getInt(1);
                ps = con.prepareStatement("ALTER TABLE platos AUTO_INCREMENT = ?");
                ps.setInt(1, maxId + 1);
                ps.execute();
            }

            // Confirmar los cambios
            con.commit();
        } catch (SQLException e) {
            System.out.println("Error al reasignar IDs: " + e.toString());
            try {
                if (con != null) {
                    con.rollback(); // Revertir cambios en caso de error
                }
            } catch (SQLException ex) {
                System.out.println("Error al hacer rollback: " + ex.toString());
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.setAutoCommit(true); // Restaurar el estado de la conexión
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    // Modificar un plato
    public boolean Modificar(Platos pla) {
        String sql = "UPDATE platos SET nombre=?, precio=? WHERE id=?";
        Connection con = null;
        try {
            con = cn.getConexion(); // Obtener una nueva conexión
            ps = con.prepareStatement(sql);
            ps.setString(1, pla.getNombre());
            ps.setDouble(2, pla.getPrecio());
            ps.setInt(3, pla.getId());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close(); // Cerrar conexión después de usarla
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }
}
