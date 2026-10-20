package br.com.ebac.anotacao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê as annotations de mapeamento das entidades.
 */
public final class AnotacaoUtil {

    private AnotacaoUtil() {
    }

    public static String getTabela(Class<?> classe) {
        Tabela tabela = classe.getAnnotation(Tabela.class);
        if (tabela == null) {
            throw new IllegalStateException("Classe " + classe.getSimpleName() + " sem @Tabela");
        }
        return tabela.value();
    }

    /** Atributos com @ColunaTabela, na ordem em que foram declarados. */
    public static List<Field> getCampos(Class<?> classe) {
        List<Field> campos = new ArrayList<>();
        for (Field campo : classe.getDeclaredFields()) {
            if (campo.isAnnotationPresent(ColunaTabela.class)) {
                campo.setAccessible(true);
                campos.add(campo);
            }
        }
        return campos;
    }

    public static Field getCampoChave(Class<?> classe) {
        for (Field campo : getCampos(classe)) {
            if (campo.isAnnotationPresent(TipoChave.class)) {
                return campo;
            }
        }
        throw new IllegalStateException("Classe " + classe.getSimpleName() + " sem @TipoChave");
    }

    public static String getColuna(Field campo) {
        return campo.getAnnotation(ColunaTabela.class).dbName();
    }

    @SuppressWarnings("unchecked")
    public static <E> E getValorChave(Object entidade) {
        return (E) getValor(getCampoChave(entidade.getClass()), entidade);
    }

    public static Object getValor(Field campo, Object entidade) {
        try {
            return campo.get(entidade);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Não foi possível ler " + campo.getName(), e);
        }
    }

    /** Preenche o atributo chamando o setter informado em @ColunaTabela. */
    public static void setValor(Field campo, Object entidade, Object valor) {
        String setter = campo.getAnnotation(ColunaTabela.class).setJavaName();
        try {
            entidade.getClass().getMethod(setter, campo.getType()).invoke(entidade, valor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Não foi possível chamar " + setter, e);
        }
    }
}
