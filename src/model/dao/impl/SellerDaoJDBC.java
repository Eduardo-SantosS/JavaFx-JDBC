package model.dao.impl;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellerDaoJDBC implements SellerDao {

    private final Connection conn;

    public SellerDaoJDBC(Connection conn){
        this.conn = conn;
    }

    @Override
    public void insert(Seller seller) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "insert into seller " +
                            "(Name, Email, BirthDate, BaseSalary, DepartmentId) " +
                            "values " +
                            "(?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, seller.getName());
            ps.setString(2, seller.getEmail());
            ps.setDate(3, new Date(seller.getBirthDate().getTime()));
            ps.setDouble(4, seller.getBaseSalary());
            ps.setInt(5, seller.getDepartment().getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0){
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()){
                    int id = rs.getInt(1);
                    seller.setId(id);
                }
                DB.closeResultSet(rs);
            }else {
                throw new DbException("Unexpected error! No rows affected");
            }
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public void update(Seller seller) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "update seller " +
                            "set Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? " +
                            "where Id = ?"
                    );

            ps.setString(1, seller.getName());
            ps.setString(2, seller.getEmail());
            ps.setDate(3, new Date(seller.getBirthDate().getTime()));
            ps.setDouble(4, seller.getBaseSalary());
            ps.setInt(5, seller.getDepartment().getId());
            ps.setInt(6, seller.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally {
            DB.closeStatement(ps);
        }

    }

    @Override
    public void deleteById(Integer id) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("delete from seller where Id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally {
            DB.closeStatement(ps);
        }

    }

    @Override
    public Seller findById(Integer id) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    "select seller.*, department.Name as DepName " +
                            "from seller " +
                            "inner join department " +
                            "on seller.DepartmentId = department.Id " +
                            "where seller.Id = ?"
            );
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()){
                Department dep = instantiateDepartment(rs);
                return instantiateSeller(rs, dep);
            }
            return null;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }
    }

    private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
        Seller seller = new Seller();
        seller.setId(rs.getInt("Id"));
        seller.setName(rs.getString("Name"));
        seller.setEmail(rs.getString("Email"));
        seller.setBaseSalary(rs.getDouble("BaseSalary"));
        seller.setBirthDate(rs.getDate("BirthDate"));
        seller.setDepartment(dep);
        return seller;
    }

    private Department instantiateDepartment(ResultSet rs) throws SQLException {
        Department dep =  new Department();
        dep.setId(rs.getInt("DepartmentId"));
        dep.setName(rs.getString("DepName"));
        return dep;
    }

    @Override
    public List<Seller> findAll() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    "select seller.*, department.Name as DepName " +
                            "from seller " +
                            "inner join department " +
                            "on seller.DepartmentId = department.Id "
            );
            rs = ps.executeQuery();

            List<Seller> list = new ArrayList<>();
            Map<Integer, Department> map = new HashMap<>();

            while (rs.next()){
                Department dep = map.get(rs.getInt("DepartmentId"));

                if (dep == null) {
                    dep = instantiateDepartment(rs);
                    map.put(rs.getInt("DepartmentId"), dep);
                }

                Seller seller = instantiateSeller(rs, dep);
                list.add(seller);
            }
            return list;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<Seller> findByDepartment(Department department) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    "select seller.*, department.Name as DepName " +
                            "from seller " +
                            "inner join department " +
                            "on seller.DepartmentId = department.Id " +
                            "where DepartmentId = ? " +
                            "order by Name"
            );
            ps.setInt(1, department.getId());
            rs = ps.executeQuery();

            List<Seller> list = new ArrayList<>();
            Map<Integer, Department> map = new HashMap<>();

            while (rs.next()){
                Department dep = map.get(rs.getInt("DepartmentId"));

                if (dep == null) {
                    dep = instantiateDepartment(rs);
                    map.put(rs.getInt("DepartmentId"), dep);
                }

                Seller seller = instantiateSeller(rs, dep);
                list.add(seller);
            }
            return list;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }
    }
}
