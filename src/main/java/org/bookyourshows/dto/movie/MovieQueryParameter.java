package org.bookyourshows.dto.movie;

public class MovieQueryParameter {

    private Integer limit;
    private Integer offset;
    private String name;
    private String language;
    private Integer releaseYear;
    private String genre;
    private String sort;

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}