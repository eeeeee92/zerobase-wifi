package com.zerobase.zerobasestudy.repository.bookmark;

import com.zerobase.zerobasestudy.entity.bookmark.Bookmark;
import com.zerobase.zerobasestudy.util.Sort;
import com.zerobase.zerobasestudy.util.exception.SqlException;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class BookmarkRepositoryJdbc implements BookmarkRepository{

    private final DataSource dataSource;
    private static final Logger LOGGER = Logger.getLogger(BookmarkRepositoryJdbc.class.getName());

    public BookmarkRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** 북마크 등록 */
    public int save(Bookmark bookmark) {
        String sql = "insert into bookmark (name, sequence_num ) "
                + "values (?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, bookmark.getName());
            stmt.setInt(2, bookmark.getSequenceNum());

            return stmt.executeUpdate();

        } catch (SQLException cause) {
            throw new SqlException(cause.getMessage(), cause);
        } finally {
            close(conn, stmt, null);
        }

    }

    /** 북마크 단건 조회 */
    public Optional<Bookmark> findById(Long id) {
        String sql = "SELECT bookmark_id, name, sequence_num, created, modified "
                + " FROM bookmark "
                + " WHERE bookmark_id = ? ";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if(rs.next()){
                return Optional.ofNullable(Bookmark.builder()
                        .id(rs.getLong("bookmark_id"))
                        .name(rs.getString("name"))
                        .sequenceNum(rs.getInt("sequence_num"))
                        .created(rs.getTimestamp("created").toLocalDateTime())
                        .modified(rs.getTimestamp("modified").toLocalDateTime())
                        .build());
            }

            return Optional.empty();

        }catch (SQLException cause) {
            throw new SqlException(cause.getMessage(), cause);
        }finally {
            close(conn, stmt, rs);
        }
    }

    /** 북마크 전체 조회 */
    public List<Bookmark> findAll(Integer limit, Sort sort) {

        String sql = "SELECT bookmark_id, name, sequence_num, created, modified FROM bookmark";

        if(sort != null) sql += " ORDER BY " + sort.getOrderBy();
        if(limit != null && limit > 0) sql += " LIMIT "  + limit;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<Bookmark> bookmarks = new ArrayList<>();
            while(rs.next()){
                Bookmark bookmark = Bookmark.builder()
                        .id(rs.getLong("bookmark_id"))
                        .name(rs.getString("name"))
                        .sequenceNum(rs.getInt("sequence_num"))
                        .created(rs.getTimestamp("created").toLocalDateTime())
                        .modified(rs.getTimestamp("modified").toLocalDateTime())
                        .build();
                bookmarks.add(bookmark);
            }
            return bookmarks;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(conn, stmt, rs);
        }

    }


    public int update(Long id, String name, Integer sequenceNum) {

        StringBuilder sql = buildDynamicSql(name, sequenceNum);

        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql.toString());

            setDynamicParameters(id, name, sequenceNum, stmt);

            return stmt.executeUpdate();

        } catch (SQLException cause) {
            throw new SqlException(cause.getMessage(), cause);
        } finally {
            close(conn, stmt, null);
        }


    }




    /** 북마크 단건 삭제 */
    public int deleteById(Long id) {
        String sql = "delete from bookmark where bookmark_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);


            return stmt.executeUpdate();


        } catch (SQLException cause) {
            throw new SqlException(cause.getMessage(), cause);
        } finally {
            close(conn, stmt, null);
        }

    }

    /** 커넥션 종료(반환) */
    private void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException cause) {
                throw new SqlException(cause.getMessage(), cause);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException cause) {
                throw new SqlException(cause.getMessage(), cause);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException cause) {
                throw new SqlException(cause.getMessage(), cause);
            }
        }
    }

    private static StringBuilder buildDynamicSql(String name, Integer sequenceNum) {
        StringBuilder sql = new StringBuilder();
        sql.append("update bookmark set ");

        if(name != null){
            sql.append(" name = ?, ");
        } else if (sequenceNum != null) {
            sql.append(" sequence_num = ?, ");
        }

        sql.append(" modified = ? ");
        sql.append(" where bookmark_id = ?");
        return sql;
    }

    private static void setDynamicParameters(Long id, String name, Integer sequenceNum, PreparedStatement stmt) throws SQLException {
        int parameterIndex = 1;

        if (name != null) {
            stmt.setString(parameterIndex++, name);
        } else if (sequenceNum != null) {
            stmt.setInt(parameterIndex++, sequenceNum);
        }

        stmt.setString(parameterIndex++, LocalDateTime.now().toString());
        stmt.setLong(parameterIndex++, id);
    }
}
